package eaglemixins.util;

/**
 * Implemented on WalkNodeProcessor by mixin. While at least one scope is open, results of
 * getPathNodeType(IBlockAccess, x, y, z) are cached per position. Scopes are only opened around
 * synchronous work during which the world cannot change (a single path search, a single pathFollow).
 */
public interface IPathNodeTypeCache {
    void eaglemixins$beginNodeCache();
    void eaglemixins$endNodeCache();
}
