/*
 * Copyright (c) 2014, 2019, Oracle and/or its affiliates. All rights reserved.
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
package jdk.graal.compiler.hotspot;

import org.graalvm.collections.EconomicMap;
import org.graalvm.collections.Equivalence;

import jdk.graal.compiler.core.common.CompilationIdentifier;
import jdk.graal.compiler.core.common.alloc.RegisterAllocationConfig;
import jdk.graal.compiler.hotspot.stubs.Stub;
import jdk.graal.compiler.lir.LIR;
import jdk.graal.compiler.lir.LIRFrameState;
import jdk.graal.compiler.lir.StandardOp;
import jdk.graal.compiler.lir.StandardOp.SaveRegistersOp;
import jdk.graal.compiler.lir.framemap.FrameMapBuilder;
import jdk.graal.compiler.lir.gen.LIRGenerationResult;
import jdk.vm.ci.code.CallingConvention;
import jdk.vm.ci.code.StackSlot;

public class HotSpotLIRGenerationResult extends LIRGenerationResult {

    /**
     * The slot reserved for storing the original return address when a frame is marked for
     * deoptimization. The return address slot in the callee is overwritten with the address of a
     * deoptimization stub.
     */
    private StackSlot deoptimizationRescueSlot;
    protected final Stub stub;
    private final boolean requiresReservedStackAccessCheck;

    private int maxInterpreterFrameSize;

    private StandardOp.SaveRegistersOp saveOnEntry;

    /**
     * Map from debug infos that need to be updated with callee save information to the operations
     * that provide the information.
     */
    private EconomicMap<LIRFrameState, SaveRegistersOp> calleeSaveInfo = EconomicMap.create(Equivalence.IDENTITY_WITH_SYSTEM_HASHCODE);

    public HotSpotLIRGenerationResult(CompilationIdentifier compilationId, LIR lir, FrameMapBuilder frameMapBuilder, RegisterAllocationConfig registerAllocationConfig,
                    CallingConvention callingConvention, Stub stub, boolean requiresReservedStackAccessCheck) {
        super(compilationId, lir, frameMapBuilder, registerAllocationConfig, callingConvention);
        this.stub = stub;
        this.requiresReservedStackAccessCheck = requiresReservedStackAccessCheck;
    }

    public void setSaveOnEntry(StandardOp.SaveRegistersOp saveOnEntry) {
        this.saveOnEntry = saveOnEntry;
    }

    public StandardOp.SaveRegistersOp getSaveOnEntry() {
        return saveOnEntry;
    }

    public EconomicMap<LIRFrameState, SaveRegistersOp> getCalleeSaveInfo() {
        return calleeSaveInfo;
    }

    public Stub getStub() {
        return stub;
    }

    public StackSlot getDeoptimizationRescueSlot() {
        return deoptimizationRescueSlot;
    }

    public final void setDeoptimizationRescueSlot(StackSlot stackSlot) {
        this.deoptimizationRescueSlot = stackSlot;
    }

    public void setMaxInterpreterFrameSize(int maxInterpreterFrameSize) {
        this.maxInterpreterFrameSize = maxInterpreterFrameSize;
    }

    public int getMaxInterpreterFrameSize() {
        return maxInterpreterFrameSize;
    }

    public boolean requiresReservedStackAccessCheck() {
        return requiresReservedStackAccessCheck;
    }
}
