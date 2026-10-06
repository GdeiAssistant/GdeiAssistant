package cn.gdeiassistant.core.express.converter;

import cn.gdeiassistant.common.pojo.entity.ExpressComment;
import cn.gdeiassistant.core.express.pojo.vo.ExpressCommentVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpressCommentConverter {

    /** 公开评论副本：不映射校园 username，保留昵称与正文 */
    ExpressCommentVO toVO(ExpressComment entity);

    List<ExpressCommentVO> toVOList(List<ExpressComment> entities);
}
