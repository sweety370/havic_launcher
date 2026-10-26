/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2026  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tungsten.fclcore.util.versioning;

/**
 * 发行版本提供策略：启动器只展示并允许安装 1.8.9、1.21.11 以及高于 1.21.11 的正式版。
 * 1.8.9 与 1.21.11 之间的版本（不含端点）以及更早的版本一律隐藏；快照、预发布、
 * 旧版本（beta/alpha 等）与无法识别的版本同样不提供。
 *
 * <p>版本比较一律走 {@link GameVersionNumber} 的语义化解析（major/minor/patch 数值比较），
 * 不做字符串比较，因此 {@code 1.21.9 < 1.21.10 < 1.21.11}、
 * 未来的年份式版本（如 {@code 26.1}）也能正确参与比较。</p>
 *
 * <p>该策略只约束「远端版本目录的展示与安装入口」；已安装版本、整合包按精确版本解析等
 * 本地/内部流程不受影响，避免破坏已有实例的启动与维护。</p>
 */
public final class GameVersionPolicy {

    /**
     * 保留的经典版本。
     */
    private static final String LEGACY_VERSION = "1.8.9";

    /**
     * 现代版本下限（含该版本）。
     */
    private static final String MODERN_MINIMUM_VERSION = "1.21.11";

    private GameVersionPolicy() {
    }

    /**
     * 判断给定 Minecraft 版本是否属于启动器提供的版本。
     *
     * <p>允许的版本须同时满足：</p>
     * <ol>
     *     <li>可解析为正式发布版本（{@link GameVersionNumber.Release}，GA，无 -pre/-rc 后缀）；</li>
     *     <li>等于 {@code 1.8.9}，或大于等于 {@code 1.21.11}。</li>
     * </ol>
     *
     * @param gameVersion 游戏版本号字符串，如 {@code "1.8.9"}、{@code "1.21.11"}
     * @return 是否提供该版本；快照/预发布/旧版本/未知版本一律返回 {@code false}
     */
    public static boolean isOfferedGameVersion(String gameVersion) {
        GameVersionNumber parsed = GameVersionNumber.asGameVersion(gameVersion);
        if (!(parsed instanceof GameVersionNumber.Release release)) {
            return false;
        }
        // 仅正式版：排除 -pre / -rc / Snapshot 后缀的同数字版本
        if (release.getEaType() != GameVersionNumber.Release.ReleaseType.GA) {
            return false;
        }
        // 1.21.11 及之后的所有正式版
        if (release.compareTo(MODERN_MINIMUM_VERSION) >= 0) {
            return true;
        }
        // 经典区仅保留 1.8.9
        return release.compareTo(LEGACY_VERSION) == 0;
    }
}
