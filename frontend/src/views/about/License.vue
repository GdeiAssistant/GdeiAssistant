<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="article-page">
    <div class="article-header">
      <span class="back-btn" @click="router.back()">返回</span>
      <h2 class="article-title">开源协议</h2>
    </div>
    <div class="weui-article">
      <h2>MIT License</h2>
      <p>Copyright (c) [2016-2023] [GdeiAssistant]</p>
      <p>Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:</p>
      <p>The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.</p>
      <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.</p>
      <h2>Anti 996 License Version 1.0 (Draft)</h2>
      <p>Copyright (c) [2016-2023] [GdeiAssistant]</p>
      <p>Permission is hereby granted to any individual or legal entity
        obtaining a copy of this licensed work (including the source code,
        documentation and/or related items, hereinafter collectively referred
        to as the "licensed work"), free of charge, to deal with the licensed
        work for any purpose, including without limitation, the rights to use,
        reproduce, modify, prepare derivative works of, distribute, publish
        and sublicense the licensed work, subject to the following conditions:</p>
      <p>1. The individual or the legal entity must conspicuously display,
        without modification, this License and the notice on each redistributed
        or derivative copy of the Licensed Work.</p>
      <p>2. The individual or the legal entity must strictly comply with all
        applicable laws, regulations, rules and standards of the jurisdiction
        relating to labor and employment where the individual is physically
        located or where the individual was born or naturalized; or where the
        legal entity is registered or is operating (whichever is stricter). In
        case that the jurisdiction has no such laws, regulations, rules and
        standards or its laws, regulations, rules and standards are
        unenforceable, the individual or the legal entity are required to
        comply with Core International Labor Standards.</p>
      <p>3. The individual or the legal entity shall not induce or force its
        employee(s), whether full-time or part-time, or its independent
        contractor(s), in any methods, to agree in oral or written form, to
        directly or indirectly restrict, weaken or relinquish his or her
        rights or remedies under such laws, regulations, rules and standards
        relating to labor and employment as mentioned above, no matter whether
        such written or oral agreement are enforceable under the laws of the
        said jurisdiction, nor shall such individual or the legal entity
        limit, in any methods, the rights of its employee(s) or independent
        contractor(s) from reporting or complaining to the copyright holder or
        relevant authorities monitoring the compliance of the license about
        its violation(s) of the said license.</p>
      <p>THE LICENSED WORK IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
        EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
        MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
        IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM,
        DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
        OTHERWISE, ARISING FROM, OUT OF OR IN ANY WAY CONNECTION WITH THE
        LICENSED WORK OR THE USE OR OTHER DEALINGS IN THE LICENSED WORK.</p>
    </div>
  </div>
</template>

<style scoped>
.article-page {
  min-height: 100vh;
  background-color: #fff;
}

.article-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 0 16px;
  z-index: 1000;
}

.back-btn {
  color: #09bb07;
  font-size: 15px;
  cursor: pointer;
  margin-right: 16px;
}

.article-title {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0;
}

.weui-article {
  margin-top: 50px;
  padding: 20px 16px;
  line-height: 1.8;
  color: #333;
  font-size: 14px;
}

.weui-article p {
  margin: 12px 0;
}

.weui-article h2 {
  font-size: 18px;
  font-weight: 600;
  margin: 24px 0 12px 0;
}
</style>
