package rs.ac.bg.etf.pp1.codegen;

import rs.ac.bg.etf.pp1.logger.CompilerLogger;
import rs.ac.bg.etf.pp1.util.Context;

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

    private int get1(int pc) {
        return buf[pc] & 0xff;
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

    public void emitop2w(int op, int od) {
        emitop(op);
        if (!alive) return;
        emit2(od);
    }

    public void enterMethod(MethodSymbol method) {
        method.setAddress(pc);
        emit1(enter);
        emit1(method.getLevel());
        emit1(method.getLocalSymbols().size());
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

    public static class Chain {
        public final int pc;
        public final Chain next;

        public Chain(int pc, Chain next) {
            this.pc = pc;
            this.next = next;
        }
    }

    public static int negate(int opcode) {
        return jcc + inverse[opcode];
    }

    public int emitJump(int opcode) {
        emitop2(opcode, 0);
        return pc - 3;
    }

    public Chain branch(int opcode) {
        return new Chain(emitJump(opcode), null);
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
}
