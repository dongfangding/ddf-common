package com.ddf.boot.common.core.util;

public class NoblePrivilegeUtil {

    public enum NoblePrivilege {
        EXP, // 送礼经验加成
        MESSAGE, // 私信
        ENTRY, // 进房隐身
        PROTECT, // 防踢防禁
        SECRET, // 神秘人
        GOODNUMBER // 贵族外显id
    }


    private final static Integer[][] privilegeArr =
            {{0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0}, {5, 1, 0, 0, 0, 0},
                    {10, 1, 1, 0, 0, 4}, {10, 1, 1, 1, 1, 3}, {20, 1, 1, 2, 1, 2}};

    /**
     * 获取贵族特权
     *
     * @param noble 贵族等级 1:骑士 2:勋爵 3:子爵 4:伯爵 5:侯爵 6:公爵 7:神皇
     * @param noblePrivilege 贵族特权
     * @return >0 拥有特权
     */
    public static int get(int noble, NoblePrivilege noblePrivilege) {
        return privilegeArr[noble][noblePrivilege.ordinal()];
    }

    /**
     * 是否有进房隐身的权限
     *
     * @param noble noble参数
     */
    public static boolean hasEntryRoomInvisiblePermission(int noble) {
        return get(noble, NoblePrivilege.ENTRY) > 0;
    }

    /**
     * 是否有神秘人的权限
     *
     * @param noble noble参数
     */
    public static boolean hasMysteryManPermission(int noble) {
        return get(noble, NoblePrivilege.SECRET) > 0;
    }

}
