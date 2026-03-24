<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <!-- Header -->
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="$router.back()" class="text-[var(--c-primary)] text-sm font-medium">← 返回</button>
      <span class="flex-1 text-center text-sm font-bold">开源协议</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-2xl mx-auto px-4 py-6">
      <div class="rounded-xl bg-[var(--c-surface)] border border-[var(--c-border)] p-5">
        <div class="text-sm leading-relaxed text-[var(--c-text-2)]">
          <h2 class="text-base font-bold mb-3 text-[var(--c-text-primary)]">MIT License</h2>
          <p class="mb-4">Copyright (c) [2016-2026] [GdeiAssistant]</p>
          <p class="mb-4">Permission is hereby granted, free of charge, to any person obtaining a copy
            of this software and associated documentation files (the "Software"), to deal
            in the Software without restriction, including without limitation the rights
            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
            copies of the Software, and to permit persons to whom the Software is
            furnished to do so, subject to the following conditions:</p>
          <p class="mb-4">The above copyright notice and this permission notice shall be included in all
            copies or substantial portions of the Software.</p>
          <p class="mb-4">THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
            SOFTWARE.</p>

          <h2 class="text-base font-bold mb-3 mt-6 text-[var(--c-text-primary)]">Anti 996 License Version 1.0 (Draft)</h2>
          <p class="mb-4">Copyright (c) [2016-2026] [GdeiAssistant]</p>
          <p class="mb-4">Permission is hereby granted to any individual or legal entity
            obtaining a copy of this licensed work (including the source code,
            documentation and/or related items, hereinafter collectively referred
            to as the "licensed work"), free of charge, to deal with the licensed
            work for any purpose, including without limitation, the rights to use,
            reproduce, modify, prepare derivative works of, distribute, publish
            and sublicense the licensed work, subject to the following conditions:</p>
          <p class="mb-4">1. The individual or the legal entity must conspicuously display,
            without modification, this License and the notice on each redistributed
            or derivative copy of the Licensed Work.</p>
          <p class="mb-4">2. The individual or the legal entity must strictly comply with all
            applicable laws, regulations, rules and standards of the jurisdiction
            relating to labor and employment where the individual is physically
            located or where the individual was born or naturalized; or where the
            legal entity is registered or is operating (whichever is stricter). In
            case that the jurisdiction has no such laws, regulations, rules and
            standards or its laws, regulations, rules and standards are
            unenforceable, the individual or the legal entity are required to
            comply with Core International Labor Standards.</p>
          <p class="mb-4">3. The individual or the legal entity shall not induce or force its
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
          <p class="mb-4">THE LICENSED WORK IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
            EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
            MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
            IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM,
            DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
            OTHERWISE, ARISING FROM, OUT OF OR IN ANY WAY CONNECTION WITH THE
            LICENSED WORK OR THE USE OR OTHER DEALINGS IN THE LICENSED WORK.</p>
        </div>
      </div>
    </div>
  </div>
</template>
