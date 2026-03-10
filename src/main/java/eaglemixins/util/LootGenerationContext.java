package eaglemixins.util;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;

public class LootGenerationContext {

    private static final ThreadLocal<Deque<ResourceLocation>> stack =
            ThreadLocal.withInitial(ArrayDeque::new);

    /** Push a loot table onto the stack, allowing duplicates for nested tables if needed */
    public static void push(ResourceLocation table) {
        stack.get().push(table);
        System.out.println("[CONTEXT PUSH] " + table + " | STACK: " + stack.get());
    }

    /** Pop the top loot table */
    public static void pop() {
        Deque<ResourceLocation> s = stack.get();
        if (!s.isEmpty()) {
            ResourceLocation popped = s.pop();
            System.out.println("[CONTEXT POP] " + popped + " | STACK: " + s);
        }
    }

    /** Get the current stack as a snapshot (read-only) */
    public static Deque<ResourceLocation> getCurrentStack() {
        return new ArrayDeque<>(stack.get());
    }

    /** Clear the stack completely */
    public static void clear() {
        stack.get().clear();
        System.out.println("[CONTEXT CLEAR] stack cleared");
    }

    /** Get a unique list of all tables in the current stack */
    public static Deque<ResourceLocation> getUniqueStack() {
        Deque<ResourceLocation> unique = new ArrayDeque<>();
        for (ResourceLocation rl : stack.get()) {
            if (!unique.contains(rl)) unique.add(rl);
        }
        return unique;
    }
}