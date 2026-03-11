package eaglemixins.util;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;

public class LootGenerationContext {

    private static final ThreadLocal<Deque<ResourceLocation>> stack =
            ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(ResourceLocation table) {
        stack.get().push(table);
    }

    public static void pop() {
        Deque<ResourceLocation> s = stack.get();
        if (!s.isEmpty()) {
            ResourceLocation popped = s.pop();
        }
    }

    public static Deque<ResourceLocation> getCurrentStack() {
        return new ArrayDeque<>(stack.get());
    }

    public static void clear() {
        stack.get().clear();
    }

    public static Deque<ResourceLocation> getUniqueStack() {
        Deque<ResourceLocation> unique = new ArrayDeque<>();
        for (ResourceLocation rl : stack.get()) {
            if (!unique.contains(rl)) unique.add(rl);
        }
        return unique;
    }
}