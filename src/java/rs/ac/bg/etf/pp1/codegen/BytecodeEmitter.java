package rs.ac.bg.etf.pp1.codegen;

import rs.ac.bg.etf.pp1.logger.CompilerLogger;
import rs.ac.bg.etf.pp1.util.Context;
import rs.etf.pp1.mj.runtime.Code;

import java.io.OutputStream;

import static rs.etf.pp1.mj.runtime.Code.*;
import static rs.ac.bg.etf.pp1.symbols.Symbol.*;

@SuppressWarnings("SpellCheckingInspection")
public final class BytecodeEmitter {
    private static final Context.Key<BytecodeEmitter> emitterKey = new Context.Key<>();
    private final CompilerLogger logger;
    private boolean alive = true;
    private Chain pendingJumps = null;

    private BytecodeEmitter(Context context) {
        context.put(emitterKey, this);
        this.logger = CompilerLogger.getInstance(context);
    }

    public static BytecodeEmitter getInstance(Context context) {
        BytecodeEmitter emitter = context.get(emitterKey);
        if (emitter == null) {
            emitter = new BytecodeEmitter(context);
        }
        return emitter;
    }

    private void emit1(int od) {
        if (!alive) return;
        if (pc >= buf.length) {
            logger.error("Code too big.");
            alive = false;
            return;
        }
        put(od);
    }

    private void emit2(int od) {
        if (!alive) return;
        if (pc + 2 > buf.length) {
            emit1(od >> 8);
            emit1(od);
        } else {
            put2(od);
        }
    }

    private void emit4(int od) {
        if (!alive) return;
        if (pc + 4 > buf.length) {
            emit1(od >> 24);
            emit1(od >> 16);
            emit1(od >> 8);
            emit1(od);
        } else {
            put4(od);
        }
    }

    private int get1(int pc) {
        return buf[pc] & 0xFF;
    }

    private void emitop(int op) {
        if (pendingJumps != null) resolvePending();
        if (!alive) return;
        emit1(op);
    }

    public void emitop0(int op) {
        emitop(op);
        if (!alive) return;
        switch (op) {
            case return_:
            case jmp:
            case trap:
                markDead();
        }
    }

    public void emitop1(int op, int od) {
        emitop(op);
        if (!alive) return;
        emit1(od);
    }

    public void emitop2(int op, int od) {
        emitop(op);
        if (!alive) return;
        emit2(od);
        if (op == jmp) markDead();
    }

    public void emitop4(int op, int od) {
        emitop(op);
        if (!alive) return;
        emit4(od);
    }

    public int emitVirtualFunction(MethodSymbol method, int nextStatic) {
        for (char c : method.getName().toCharArray()) {
            emitop4(const_, c);
            emitop2(putstatic, nextStatic++);
        }
        emitop0(const_m1);
        emitop2(putstatic, nextStatic++);
        emitop4(const_, method.getAddress());
        emitop2(invokevirtual, nextStatic++);

        return nextStatic;
    }

    public int emitVFTEntryEnd(int nextStatic) {
        emitop4(const_, -2);
        emitop2(putstatic, nextStatic++);
        return nextStatic;
    }

    public void emitInvokevirtual(String name) {
        emit1(invokevirtual);
        for (char c : name.toCharArray()) emit1(c);
        emit1(const_m1);
    }

    public boolean isAlive() {
        return alive || pendingJumps != null;
    }

    public void markDead() {
        alive = false;
    }

    public int entryPoint() {
        int currentPc = pc;
        alive = true;
        return currentPc;
    }

    public int currentPC() {
        return pc;
    }

    public void setMain() {
        mainPc = pc;
    }

    public void emitCall(int address) {
        emitop2(call, address - pc);
    }

    public Chain emitCall(Chain chain) {
        emitop2(call, 0);
        return mergeChains(chain, new Chain(pc - 3, null));
    }

    public static class Chain {
        public final int pc;
        public final Chain next;

        public Chain(int pc, Chain next) {
            this.pc = pc;
            this.next = next;
        }
    }

    public static int negate(int opcode) {
        return ((opcode + 1) ^ 1) - 1;
    }

    public int emitJump(int opcode) {
        emitop2(opcode, 0);
        return pc - 3;
    }

    public Chain branch(int opcode) {
        Chain result = null;
        if (opcode == jmp) {
            result = pendingJumps;
            pendingJumps = null;
        }
        if (opcode != Bytecodes.dontjmp && isAlive()) {
            result = new Chain(emitJump(opcode), result);
            if (opcode == jmp) alive = false;
        }
        return result;
    }

    public void resolve(Chain chain, int target) {
        while (chain != null) {
            if (get1(chain.pc) == jmp && chain.pc + 3 == target && target == pc) {
                pc = pc - 3;
                target = target - 3;
                if (chain.next == null) {
                    alive = true;
                    break;
                }
            } else {
                put2(chain.pc + 1, target - chain.pc);
                chain = chain.next;
            }
            if (pc == target) {
                alive = true;
            }
        }
    }

    public void resolve(Chain chain) {
        pendingJumps = mergeChains(chain, pendingJumps);
    }

    public void resolvePending() {
        Chain x = pendingJumps;
        pendingJumps = null;
        resolve(x, pc);
    }

    public static Chain mergeChains(Chain chain1, Chain chain2) {
        if (chain1 == null) return chain2;
        if (chain2 == null) return chain1;

        if (chain1.pc < chain2.pc) return new Chain(chain2.pc, mergeChains(chain1, chain2.next));
        else return new Chain(chain1.pc, mergeChains(chain1.next, chain2));
    }

    public void setDataSize(int size) {
        dataSize = size;
    }

    public int getDataSize() {
        return dataSize;
    }

    public static void reset() {
        buf = new byte[buf.length];
        pc = 0;
        mainPc = -1;
        dataSize = 0;
        greska = false;
    }

    public static void write(OutputStream os) {
        Code.write(os);
    }
}
