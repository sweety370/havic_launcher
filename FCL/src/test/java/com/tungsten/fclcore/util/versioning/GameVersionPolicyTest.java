package com.tungsten.fclcore.util.versioning;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * {@link GameVersionPolicy} 发行策略验证：只提供 1.8.9、1.21.11 及高于 1.21.11 的正式版，
 * 版本比较必须走语义化解析（1.21.9 < 1.21.10 < 1.21.11），不能退化为字符串比较。
 */
public class GameVersionPolicyTest {

    // ---- 提供的版本 ----

    @Test
    public void legacyAnchorIsOffered() {
        assertTrue(GameVersionPolicy.isOfferedGameVersion("1.8.9"));
    }

    @Test
    public void modernAnchorIsOffered() {
        assertTrue(GameVersionPolicy.isOfferedGameVersion("1.21.11"));
    }

    @Test
    public void releasesNewerThanModernAnchorAreOffered() {
        assertTrue(GameVersionPolicy.isOfferedGameVersion("1.21.12"));
        assertTrue(GameVersionPolicy.isOfferedGameVersion("1.22"));
        assertTrue(GameVersionPolicy.isOfferedGameVersion("1.99.1"));
        // 年份式版本（26.x 起）同样按数值比较判为更新
        assertTrue(GameVersionPolicy.isOfferedGameVersion("26.1"));
        assertTrue(GameVersionPolicy.isOfferedGameVersion("27.2.3"));
    }

    // ---- 隐藏的版本：1.8.9 与 1.21.11 之间 ----

    @Test
    public void versionsBetweenAnchorsAreHidden() {
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.8.10"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.9"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.12.2"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.16.5"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.20.1"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.21"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.21.10"));
    }

    /** 数字位数不同的版本必须按数值比较：1.21.9 应小于 1.21.11，而非字符串意义上的更大 */
    @Test
    public void comparisonIsNumericNotLexicographic() {
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.21.9"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.21.10"));
    }

    // ---- 隐藏的版本：早于 1.8.9 ----

    @Test
    public void versionsOlderThanLegacyAnchorAreHidden() {
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.8"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.8.8"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.7.10"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.5.2"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.0"));
    }

    // ---- 快照 / 预发布 / 旧版本 / 未知版本一律隐藏 ----

    @Test
    public void snapshotsAreHidden() {
        assertFalse(GameVersionPolicy.isOfferedGameVersion("24w14a"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("25w14craftmine"));
    }

    @Test
    public void preReleasesAndReleaseCandidatesAreHidden() {
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.21.11-pre1"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.21.12-rc1"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.22-pre1"));
    }

    @Test
    public void oldBetaAlphaVersionsAreHidden() {
        assertFalse(GameVersionPolicy.isOfferedGameVersion("b1.7.3"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("a1.2.6"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("rd-132211"));
    }

    @Test
    public void unknownOrInvalidVersionsAreHidden() {
        assertFalse(GameVersionPolicy.isOfferedGameVersion(""));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("custom-version"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("1.21.11-garbage"));
        assertFalse(GameVersionPolicy.isOfferedGameVersion("0.0"));
    }
}
