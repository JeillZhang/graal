/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.graal.compiler.hotspot.aarch64;

import static jdk.vm.ci.hotspot.aarch64.AArch64HotSpotRegisterConfig.inlineCacheRegister;

import jdk.graal.compiler.asm.aarch64.AArch64MacroAssembler;
import jdk.graal.compiler.hotspot.GraalHotSpotVMConfig;
import jdk.graal.compiler.hotspot.HotSpotMarkId;
import jdk.graal.compiler.lir.LIRFrameState;
import jdk.graal.compiler.lir.LIRInstructionClass;
import jdk.graal.compiler.lir.Opcode;
import jdk.graal.compiler.lir.aarch64.AArch64Call;
import jdk.graal.compiler.lir.aarch64.AArch64Call.DirectCallOp;
import jdk.graal.compiler.lir.asm.CompilationResultBuilder;
import jdk.graal.compiler.nodes.CallTargetNode.InvokeKind;

import jdk.vm.ci.hotspot.HotSpotResolvedJavaMethod;
import jdk.vm.ci.meta.ResolvedJavaMethod;
import jdk.vm.ci.meta.Value;

/**
 * A direct call that complies with the conventions for such calls in HotSpot. In particular, for
 * calls using an inline cache, a MOVE instruction is emitted just prior to the aligned direct call.
 */
@Opcode("CALL_DIRECT")
final class AArch64HotSpotDirectVirtualCallOp extends DirectCallOp {

    public static final LIRInstructionClass<AArch64HotSpotDirectVirtualCallOp> TYPE = LIRInstructionClass.create(AArch64HotSpotDirectVirtualCallOp.class);

    private final InvokeKind invokeKind;
    private final GraalHotSpotVMConfig config;

    AArch64HotSpotDirectVirtualCallOp(ResolvedJavaMethod target, Value result, Value[] parameters, Value[] temps, LIRFrameState state, InvokeKind invokeKind, GraalHotSpotVMConfig config) {
        super(TYPE, target, result, parameters, temps, state);
        assert invokeKind.isIndirect();
        this.invokeKind = invokeKind;
        this.config = config;
    }

    @Override
    @SuppressWarnings("try")
    public void emitCode(CompilationResultBuilder crb, AArch64MacroAssembler masm) {
        // The mark for an invocation that uses an inline cache must be placed at the
        // instruction that loads the Klass from the inline cache.
        // For the first invocation this is set to a bitpattern that is guaranteed to never be a
        // valid object which causes the called function to call a handler that installs the
        // correct inline cache value here.
        crb.recordMark(invokeKind == InvokeKind.Virtual ? HotSpotMarkId.INVOKEVIRTUAL : HotSpotMarkId.INVOKEINTERFACE);
        masm.movNativeAddress(inlineCacheRegister, config.nonOopBits);
        if (config.supportsMethodHandleDeoptimizationEntry() && config.isMethodHandleCall((HotSpotResolvedJavaMethod) callTarget)) {
            crb.setNeedsMHDeoptHandler();
        }
        AArch64Call.directCall(crb, masm, callTarget, null, state);
    }
}
