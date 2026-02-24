package rs.ac.bg.etf.pp1.codegen;

import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.symbols.Symbol;
import rs.ac.bg.etf.pp1.symbols.Symbol.*;
import rs.ac.bg.etf.pp1.symbols.SymbolTable;
import rs.ac.bg.etf.pp1.symbols.Type;
import rs.ac.bg.etf.pp1.util.Context;

import static rs.ac.bg.etf.pp1.codegen.Bytecodes.*;
import static rs.ac.bg.etf.pp1.codegen.BytecodeEmitter.*;

public final class Items {
    BytecodeEmitter code;
    SymbolTable table;

    private final Item voidItem = new Item(voidCode) {};
    private final Item thisItem = new SelfItem();
    private final Item[] stackItem = new Item[TypeCodeCount];

    public Items(Context context) {
        this.code = BytecodeEmitter.getInstance(context);
        this.table = SymbolTable.getInstance(context);
        for (int i = 0; i < voidCode; i++) stackItem[i] = new StackItem(i);
        stackItem[voidCode] = voidItem;
    }

    Item makeVoidItem() {
        return voidItem;
    }

    Item makeThisItem() {
        return thisItem;
    }

    Item makeIndexedItem(Type type) {
        return new IndexedItem(type);
    }

    Item makeImmediateItem(int value) {
        return new ImmediateItem(value);
    }

    Item makeStaticItem(Symbol symbol) {
        return new StaticItem(symbol);
    }

    LocalItem makeLocalItem(Symbol symbol) {
        return new LocalItem(symbol);
    }

    Item makeMemberItem(Symbol symbol) {
        return new MemberItem(symbol);
    }

    Item makeAssignItem(Item item) {
        return new AssignItem(item);
    }

    ConditionalItem makeConditionalItem(int opcode, Chain trueJumps, Chain falseJumps) {
        return new ConditionalItem(opcode, trueJumps, falseJumps);
    }

    ConditionalItem makeConditionalItem(int opcode) {
        return new ConditionalItem(opcode, null, null);
    }

    public Item makeStackItem(Type type) {
        return new StackItem(type.getMJKind().getTypecode());
    }

    abstract class Item {
        int typecode;

        Item(int typecode) {
            this.typecode = typecode;
        }

        Item load() {
            throw new AssertionError();
        }
        void store() {
            throw new AssertionError();
        }
        Item invoke() {
            throw new AssertionError();
        }

        void duplicate() {}

        void drop() {}

        void stash(int toscode) {
            stackItem[toscode].duplicate();
        }

        ConditionalItem makeConditional() {
            load();
            return makeConditionalItem(ifne);
        }
    }

    final class StackItem extends Item {

        StackItem(int typecode) {
            super(typecode);
        }

        @Override
        Item load() {
            return this;
        }

        @Override
        void duplicate() {
            code.emitop0(dup);
        }

        @Override
        void drop() {
            code.emitop0(pop);
        }
    }

    final class IndexedItem extends Item {
        IndexedItem(Type type) {
            super(type.getMJKind().getTypecode());
        }

        @Override
        Item load() {
            code.emitop0(aload);
            return stackItem[typecode];
        }

        @Override
        void store() {
            code.emitop0(astore);
        }

        @Override
        void duplicate() {
            code.emitop0(dup2);
        }

        @Override
        void drop() {
            code.emitop0(pop);
            code.emitop0(pop);
        }

        @Override
        void stash(int toscode) {
            code.emitop0(dup_x2);
        }
    }

    final class SelfItem extends Item {
        SelfItem() {
            super(objectCode);
        }

        @Override
        Item load() {
            code.emitop0(load_n);
            return stackItem[typecode];
        }
    }

    final class LocalItem extends Item {
        private final int position;

        LocalItem(Symbol symbol) {
            super(symbol.getSymbolType().getMJKind().getTypecode());
            this.position = symbol.getAddress();
        }

        @Override
        Item load() {
            if (position <= 3) {
                code.emitop0(load_n + position);
            } else {
                code.emitop2(load, position);
            }
            return stackItem[typecode];
        }

        @Override
        void store() {
            if (position <= 3) {
                code.emitop0(store_0 + position);
            } else {
                code.emitop2(store, position);
            }
        }

        void increment(int value) {
            if (typecode == intCode && value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                code.emitop2(inc, (position << 8) | (value & 0xff));
            } else {
                load();
                if (value >= 0) {
                    makeImmediateItem(value).load();
                    code.emitop0(add);
                } else {
                    makeImmediateItem(-value).load();
                    code.emitop0(sub);
                }
                store();
            }
        }
    }

    final class MemberItem extends Item {
        private final Symbol symbol;

        MemberItem(Symbol symbol) {
            super(symbol.getSymbolType().getMJKind().getTypecode());
            this.symbol = symbol;
        }

        @Override
        Item load() {
            code.emitop2(getfield, symbol.getAddress());
            return stackItem[typecode];
        }

        @Override
        void store() {
            code.emitop2(putfield, symbol.getAddress());
        }

        Item invoke() {
            load();
            code.emitInvokevirtual(symbol.getName());
            return stackItem[symbol.getSymbolType().getMJKind().getTypecode()];
        }
    }

    final class ImmediateItem extends Item {
        final int value;

        ImmediateItem(int value) {
            super(intCode);
            this.value = value;
        }

        @Override
        Item load() {
            if (value <= 5 && value >= 0) {
                code.emitop0(const_0 + value);
            } else if (value == -1) {
                code.emitop0(const_m1);
            } else {
                code.emitop4(const_, value);
            }
            return stackItem[typecode];
        }

        @Override
        public ConditionalItem makeConditional() {
            return makeConditionalItem(value != 0 ? jmp : dontjmp);
        }
    }

    final class StaticItem extends Item {
        private final Symbol symbol;

        StaticItem(Symbol symbol) {
            super(symbol.getSymbolType().getMJKind().getTypecode());
            this.symbol = symbol;
        }

        @Override
        Item load() {
            code.emitop2(getstatic, symbol.getAddress());
            return stackItem[typecode];
        }

        @Override
        void store() {
            code.emitop2(putstatic, symbol.getAddress());
        }

        Item invoke() {
            if (!(symbol instanceof MethodSymbol)) {
                throw new AssertionError("Expected method symbol");
            }

            MethodSymbol method = (MethodSymbol) symbol;
            if (method.isDefined()) {
                code.emitCall(method.getAddress());
            } else {
                method.forwardReference = code.emitCall(method.forwardReference);
            }

            return stackItem[symbol.getSymbolType().getMJKind().getTypecode()];
        }
    }

    final class AssignItem extends Item {
        private final Item lhs;

        AssignItem(Item lhs) {
            super(voidCode);
            this.lhs = lhs;
        }

        @Override
        Item load() {
            lhs.stash(typecode);
            lhs.store();
            return voidItem;
        }

        @Override
        void duplicate() {
            load().duplicate();
        }

        @Override
        void drop() {
            lhs.store();
        }

        @Override
        void stash(int toscode) {
            throw new AssertionError();
        }
    }

    /**
     * Represents a conditional or unconditional jump.
     */
    final class
    ConditionalItem extends Item {
        Chain trueJumps;
        Chain falseJumps;
        int opcode;

        SyntaxNode tree;

        ConditionalItem(int opcode, Chain trueJumps, Chain falseJumps) {
            super(byteCode);
            this.opcode = opcode;
            this.trueJumps = trueJumps;
            this.falseJumps = falseJumps;
        }

        @Override
        Item load() {
            Chain trueChain = null;
            Chain falseChain = jumpFalse();
            if (!isFalse()) {
                code.resolve(trueJumps);
                code.emitop0(const_1);
                trueChain = code.branch(jmp);
            }
            if (falseChain != null) {
                code.resolve(falseChain);
                code.emitop0(const_0);
            }
            code.resolve(trueChain);
            return stackItem[typecode];
        }

        @Override
        void duplicate() {
            load().duplicate();
        }

        @Override
        void drop() {
            load().drop();
        }

        Chain jumpTrue() {
            return mergeChains(trueJumps, code.branch(opcode));
        }

        Chain jumpFalse() {
            return mergeChains(falseJumps, code.branch(BytecodeEmitter.negate(opcode)));
        }

        boolean isTrue() {
            return falseJumps == null && opcode == jmp;
        }

        boolean isFalse() {
            return trueJumps == null && opcode == dontjmp;
        }

        @Override
        public ConditionalItem makeConditional() {
            return this;
        }
    }
}
