package cn.gdeiassistant.tooling;

import com.google.gson.GsonBuilder;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/** Generate the checked contract from compiled controller signatures, not a second route list. */
public final class GenerateOpenApi {
    private final Map<String,Object> schemas=new TreeMap<>();
    private final Map<String,Object> paths=new TreeMap<>();

    public static void main(String[] args) throws Exception {
        var generator=new GenerateOpenApi();
        var document=generator.generate();
        String json=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(canonical(document))+"\n";
        Path target=Path.of("docs/openapi.yaml"); // JSON is valid YAML; one machine-readable source.
        if(args.length>0 && args[0].equals("--check")) {
            if(!Files.exists(target) || !Files.readString(target).equals(json)) throw new IllegalStateException("OpenAPI differs from compiled controllers; run ./gradlew generateOpenApi");
        } else { Files.createDirectories(target.getParent());Files.writeString(target,json); }
        System.out.println("OpenAPI verified: "+generator.paths.size()+" paths, "+generator.schemas.size()+" schemas");
    }

    private static Object canonical(Object value) {
        if(value instanceof Map<?,?> map) {
            Map<String,Object> result=new TreeMap<>();map.forEach((key,item)->result.put(String.valueOf(key),canonical(item)));return result;
        }
        if(value instanceof Collection<?> list)return list.stream().map(GenerateOpenApi::canonical).toList();
        return value;
    }

    private Map<String,Object> generate() throws Exception {
        var scanner=new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<String> names=scanner.findCandidateComponents("cn.gdeiassistant").stream().map(b->b.getBeanClassName()).filter(name -> Files.exists(Path.of("src/main/java/"+name.replace('.','/')+".java"))).sorted().toList();
        for(String name:names) {
            Class<?> controller=Class.forName(name);
            var prefix=AnnotatedElementUtils.findMergedAnnotation(controller,RequestMapping.class);
            String[] bases=prefix==null || prefix.value().length==0?new String[]{""}:prefix.value();
            for(Method method:Arrays.stream(controller.getDeclaredMethods()).sorted(Comparator.comparing(Method::toGenericString)).toList()) {
                var mapping=AnnotatedElementUtils.findMergedAnnotation(method,RequestMapping.class);
                if(mapping==null)continue;
                String[] suffixes=mapping.value().length==0?new String[]{""}:mapping.value();
                var verbs=mapping.method().length==0?RequestMethod.values():mapping.method();
                for(String base:bases)for(String suffix:suffixes)for(RequestMethod verb:verbs) {
                    String path=(base+suffix).replaceAll("/{2,}","/");
                    if(!path.startsWith("/api/"))continue;
                    @SuppressWarnings("unchecked") Map<String,Object> operations=(Map<String,Object>)paths.computeIfAbsent(path,p->new TreeMap<>());
                    String key=verb.name().toLowerCase(Locale.ROOT);
                    Map<String,Object> operation=operation(controller,method,mapping,verb);
                    if (Arrays.stream(cn.gdeiassistant.common.constant.SettingConstantUtils.LOGIN_INTERCEPTOR_EXCEPTION_LIST).anyMatch(path::startsWith) || path.startsWith("/api/social/users/") && path.endsWith("/avatar")) operation.put("security", List.of());
                    operation.put("operationId",controller.getSimpleName()+"_"+method.getName()+"_"+key+"_"+path.replaceAll("[^a-zA-Z0-9]","_"));
                    if(operations.putIfAbsent(key,operation)!=null)throw new IllegalStateException("Duplicate API operation: "+key+" "+path);
                }
            }
        }
        return Map.of("openapi","3.0.3","info",Map.of("title","GdeiAssistant API","version","2026.10.07"),
                "servers",List.of(Map.of("url","/")),"paths",paths,
                "components",Map.of("schemas",schemas,"securitySchemes",Map.of("bearerAuth",Map.of("type","http","scheme","bearer","bearerFormat","JWT"))));
    }

    private Map<String,Object> operation(Class<?> controller,Method method,RequestMapping mapping,RequestMethod verb) {
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("tags",List.of(controller.getSimpleName().replace("Controller","")));
        result.put("summary",method.getName());
        List<Object> parameters=new ArrayList<>();Map<String,Object> form=new TreeMap<>();List<String> required=new ArrayList<>();
        Object body=null;boolean multipart=false;
        for(Parameter parameter:method.getParameters()) {
            Class<?> raw=parameter.getType();
            if(raw.getName().startsWith("jakarta.servlet.") || raw.getName().startsWith("java.security."))continue;
            RequestBody requestBody=parameter.getAnnotation(RequestBody.class);
            if(requestBody!=null){body=Map.of("required",requestBody.required(),"content",Map.of("application/json",Map.of("schema",schema(parameter.getParameterizedType(),Map.of()))));continue;}
            PathVariable variable=parameter.getAnnotation(PathVariable.class);
            RequestParam query=parameter.getAnnotation(RequestParam.class);
            RequestHeader header=parameter.getAnnotation(RequestHeader.class);
            RequestPart part=parameter.getAnnotation(RequestPart.class);
            boolean file=raw==MultipartFile.class || raw==MultipartFile[].class;
            String name=variable!=null?named(variable.value(),variable.name(),parameter):query!=null?named(query.value(),query.name(),parameter):header!=null?named(header.value(),header.name(),parameter):part!=null?named(part.value(),part.name(),parameter):parameter.getName();
            Object shape=schema(parameter.getParameterizedType(),Map.of());
            if(variable!=null || header!=null || (query!=null && !file)) {
                Map<String,Object> item=new LinkedHashMap<>();item.put("name",name);item.put("in",variable!=null?"path":header!=null?"header":"query");
                item.put("required",variable!=null || (header!=null?header.required():query.required() && query.defaultValue().equals(ValueConstants.DEFAULT_NONE)));
                item.put("schema",shape);parameters.add(item);
            } else if(file || part!=null) { multipart=true;form.put(name,shape);if(part!=null && part.required())required.add(name); }
            else if(raw.getName().startsWith("cn.gdeiassistant.")) {
                var fields=properties(raw,Map.of());form.putAll(fields);
                for(Field f:allFields(raw)) if(isRequired(f.getAnnotations()))required.add(f.getName());
            } else {
                // Unannotated scalar parameters bind from form fields (or GET query).
                if(verb==RequestMethod.GET)parameters.add(Map.of("name",name,"in","query","required",false,"schema",shape));
                else form.put(name,shape);
            }
        }
        if(!parameters.isEmpty())result.put("parameters",parameters);
        if(body!=null)result.put("requestBody",body);
        else if(!form.isEmpty()) {
            Map<String,Object> formSchema=new LinkedHashMap<>();formSchema.put("type","object");formSchema.put("properties",form);
            if(!required.isEmpty())formSchema.put("required",required.stream().distinct().sorted().toList());
            result.put("requestBody",Map.of("content",Map.of(multipart?"multipart/form-data":"application/x-www-form-urlencoded",Map.of("schema",formSchema))));
        }
        Type responseType=method.getGenericReturnType();
        if(responseType instanceof ParameterizedType p && p.getRawType().getTypeName().equals("org.springframework.http.ResponseEntity"))responseType=p.getActualTypeArguments()[0];
        Object responseSchema=schema(responseType,Map.of());
        String media=mapping.produces().length>0?mapping.produces()[0]:"application/json";
        Map<String,Object> responses=new TreeMap<>();responses.put("200",Map.of("description","Successful HTTP response; inspect success/errorCode in the envelope","content",Map.of(media,Map.of("schema",responseSchema))));
        for(String status:List.of("400","401","403","429","500"))responses.put(status,Map.of("description","Request, authentication, authorization, rate-limit or server failure","content",Map.of("application/json",Map.of("schema",schema(cn.gdeiassistant.common.pojo.result.JsonResult.class,Map.of())))));
        result.put("responses",responses);
        result.put("security",List.of(Map.of("bearerAuth",List.of())));
        return result;
    }

    private String named(String value,String name,Parameter parameter){return !value.isEmpty()?value:!name.isEmpty()?name:parameter.getName();}
    private Object schema(Type type,Map<TypeVariable<?>,Type> bindings) {
        if(type instanceof TypeVariable<?> t)return schema(bindings.getOrDefault(t,Object.class),bindings);
        if(type instanceof WildcardType)return Map.of("type","object");
        if(type instanceof GenericArrayType t)return Map.of("type","array","items",schema(t.getGenericComponentType(),bindings));
        if(type instanceof ParameterizedType p) {
            Class<?> raw=(Class<?>)p.getRawType();Type[] args=p.getActualTypeArguments();
            if(Collection.class.isAssignableFrom(raw))return Map.of("type","array","items",schema(args[0],bindings));
            if(Map.class.isAssignableFrom(raw))return Map.of("type","object","additionalProperties",schema(args[args.length-1],bindings));
            Map<TypeVariable<?>,Type> resolved=new HashMap<>(bindings);var variables=raw.getTypeParameters();
            for(int i=0;i<variables.length;i++)resolved.put(variables[i],args[i]);
            return objectSchema(raw,resolved,typeName(raw)+"_"+Arrays.stream(args).map(this::typeName).reduce((a,b)->a+"_"+b).orElse(""));
        }
        Class<?> raw=(Class<?>)type;
        if(raw==void.class || raw==Void.class)return Map.of("type","object","nullable",true);
        if(raw==MultipartFile.class || raw==byte[].class)return Map.of("type","string","format","binary");
        if(raw.isArray())return Map.of("type","array","items",schema(raw.getComponentType(),bindings));
        if(raw==String.class || raw==Character.class || raw==char.class || raw==java.util.UUID.class)return Map.of("type","string");
        if(raw==boolean.class || raw==Boolean.class)return Map.of("type","boolean");
        if(raw==int.class || raw==Integer.class || raw==long.class || raw==Long.class || raw==short.class || raw==Short.class)return Map.of("type","integer","format",raw==long.class || raw==Long.class?"int64":"int32");
        if(Number.class.isAssignableFrom(raw) || raw==float.class || raw==double.class)return Map.of("type","number");
        if(Date.class.isAssignableFrom(raw) || raw.getName().startsWith("java.time."))return Map.of("type","string","description","Configured date/time text in Asia/Shanghai");
        if(raw.isEnum())return Map.of("type","string","enum",Arrays.stream(raw.getEnumConstants()).map(Object::toString).toList());
        if(raw==Object.class || !raw.getName().startsWith("cn.gdeiassistant."))return Map.of("type","object");
        return objectSchema(raw,bindings,typeName(raw));
    }
    private String typeName(Type t){return t.getTypeName().replace("cn.gdeiassistant.","").replaceAll("[^a-zA-Z0-9]","_");}
    private Object objectSchema(Class<?> type,Map<TypeVariable<?>,Type> bindings,String name) {
        if(!schemas.containsKey(name)) {
            schemas.put(name,new LinkedHashMap<>());
            Map<String,Object> object=new LinkedHashMap<>();object.put("type","object");object.put("properties",properties(type,bindings));
            List<String> required=allFields(type).stream().filter(f->!Modifier.isStatic(f.getModifiers()) && isRequired(f.getAnnotations())).map(Field::getName).sorted().toList();
            if(!required.isEmpty())object.put("required",required);
            schemas.put(name,object);
        }
        return Map.of("$ref","#/components/schemas/"+name);
    }
    private List<Field> allFields(Class<?> type) {
        List<Field> result=new ArrayList<>();for(Class<?> c=type;c!=null && c!=Object.class;c=c.getSuperclass())result.addAll(Arrays.asList(c.getDeclaredFields()));return result;
    }
    private Map<String,Object> properties(Class<?> type,Map<TypeVariable<?>,Type> bindings) {
        Map<String,Object> result=new TreeMap<>();
        for(Field field:allFields(type)) {
            if(Modifier.isStatic(field.getModifiers()) || field.isSynthetic() || field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonIgnore.class))continue;
            String name=field.getName();var property=field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);if(property!=null && !property.value().isEmpty())name=property.value();
            @SuppressWarnings("unchecked") Map<String,Object> shape=new LinkedHashMap<>((Map<String,Object>)schema(field.getGenericType(),bindings));
            var length=field.getAnnotation(Length.class);var size=field.getAnnotation(Size.class);
            if(length!=null){shape.put("minLength",length.min());if(length.max()!=Integer.MAX_VALUE)shape.put("maxLength",length.max());}
            if(size!=null){String kind=Collection.class.isAssignableFrom(field.getType())?"Items":"Length";shape.put("min"+kind,size.min());if(size.max()!=Integer.MAX_VALUE)shape.put("max"+kind,size.max());}
            var min=field.getAnnotation(Min.class);var max=field.getAnnotation(Max.class);var dmin=field.getAnnotation(DecimalMin.class);var dmax=field.getAnnotation(DecimalMax.class);
            if(min!=null)shape.put("minimum",min.value());if(max!=null)shape.put("maximum",max.value());if(dmin!=null)shape.put("minimum",new java.math.BigDecimal(dmin.value()));if(dmax!=null)shape.put("maximum",new java.math.BigDecimal(dmax.value()));
            var pattern=field.getAnnotation(Pattern.class);if(pattern!=null)shape.put("pattern",pattern.regexp());
            result.put(name,shape);
        }
        return result;
    }
    private boolean isRequired(java.lang.annotation.Annotation[] annotations) {
        return Arrays.stream(annotations).anyMatch(a->a instanceof NotNull || a instanceof NotBlank || a instanceof NotEmpty);
    }
}
