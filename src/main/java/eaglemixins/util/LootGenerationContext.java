package eaglemixins.util;

import net.minecraft.util.ResourceLocation;

import java.util.Deque;
import java.util.LinkedList;

public class LootGenerationContext {

    private static final ThreadLocal<Deque<ResourceLocation>> stack = ThreadLocal.withInitial(LinkedList::new);

    public static void push(ResourceLocation table) {
        stack.get().push(table);
    }

    public static void pop() {
        Deque<ResourceLocation> s = stack.get();
        if (!s.isEmpty()) s.pop();
    }

    public static Deque<ResourceLocation> getCurrentStack() {
        return stack.get();
    }

    public static void clear() {
        stack.get().clear();
    }
}