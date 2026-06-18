package com.pekar.callofcompanions.clientaccess;

public final class ClientAccessor
{
    private static INetworkClientAccessor networkClientAccessor;
    private static IItemsClientAccessor itemsClientAccessor;

    public static void init(
            INetworkClientAccessor networkAccessor,
            IItemsClientAccessor itemsAccessor
    )
    {
        networkClientAccessor = networkAccessor;
        itemsClientAccessor = itemsAccessor;
    }

    public static INetworkClientAccessor networkAccessor()
    {
        return networkClientAccessor;
    }

    public static IItemsClientAccessor itemsAccessor()
    {
        return itemsClientAccessor;
    }
}
