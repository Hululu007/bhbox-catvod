package com.github.catvod.utils;

import com.github.catvod.Init;

import java.io.File;

/**
 * 文件路径统一转换工具（全项目唯一入口）。
 *
 * 术语约定：
 * - clanPath：内置存储路径的缩写形态，canonical 为单斜杠 file:/x（如 file:/Movies/a.mp4）
 * - 绝对路径：/storage/emulated/0/x
 * - 本地文件 URL：file://x（双斜杠，供 clanToAddress/本地源链路消费）
 */
public final class ClanPath {

    private static final String CLAN_PREFIX = "file:/";
    private static final String SD_ROOT = Path.getSdRootPath();

    private ClanPath() {
    }

    // ══════ file:/ ↔ 绝对路径（收编 FilePicker.toClanPath / Io.getLocal / JarLoader.local）══════

    /** abs → clanPath（canonical 单斜杠 file:/x）；已是 clanPath 则幂等返回；非内置存储原样 */
    public static String toClanPath(String absPath) {
        if (absPath == null) return null;
        if (absPath.startsWith(CLAN_PREFIX)) return absPath;
        return absPath.replace(SD_ROOT, CLAN_PREFIX);
    }

    public static String toClanPath(File file) {
        return file == null ? null : toClanPath(file.getAbsolutePath());
    }

    /**
     * clanPath → 绝对路径。兼容双形态输入，非 clanPath 原样返回（no-op）：
     * file://x → /storage/emulated/0/x     file:/x → /storage/emulated/0/x
     */
    public static String toAbsPath(String path) {
        if (path == null || !path.startsWith(CLAN_PREFIX)) return path;
        String rest = path.substring(CLAN_PREFIX.length());
        if (rest.startsWith("/")) rest = rest.substring(1);
        return SD_ROOT + "/" + rest;
    }

    /** toAbsPath + File（收编 Io.getLocal / JarLoader.local 的确定化版本，无三重回退） */
    public static File toAbsFile(String path) {
        return new File(toAbsPath(path));
    }

    /** clan://localhost/x 或 file://x → 本地绝对路径；其余 no-op（写回本地源文件场景） */
    public static String toLocalFilePath(String path) {
        if (path == null) return null;
        if (path.startsWith("clan://localhost/")) return toAbsPath("file://" + path.substring("clan://localhost/".length()));
        return toAbsPath(path);
    }

    public static boolean isClanPath(String path) {
        return path != null && path.startsWith(CLAN_PREFIX);
    }

    // ══════ 本地文件 URL 构造（收编 DetailActivity / LocalSiteHelper 拼接）══════

    /** abs → file://<abs>（双斜杠形态，供 clanToAddress/convert 链路消费）；已是 file: 开头则原样 */
    public static String toLocalFileUrl(String absPath) {
        if (absPath == null) return null;
        return absPath.startsWith("file:") ? absPath : "file://" + absPath;
    }

    /** 本地文件 URL → clan 链接（file://x → clan://localhost/x，源配置 clan 化）；其余原样 */
    public static String toClanLink(String fileUrl) {
        if (fileUrl == null) return null;
        return fileUrl.startsWith("file://") ? fileUrl.replace("file://", "clan://localhost/") : fileUrl;
    }

    // ══════ 协议地址归一（收编 Util.clanToAddress / UrlHelper.convert / Action）══════

    /**
     * clan://localhost/x → server/file/x；clan://host/x → http://host/file/x；
     * http://127.0.0.1（历史端口/目录）重写到当前 server；file://x → server/file/x；
     * 其余原样返回。（实现整体迁自 Util.clanToAddress，行为不变）
     */
    public static String clanToAddress(String lanLink) {
        if (lanLink.startsWith("clan")) {
            if (lanLink.startsWith("clan://localhost/")) {
                return lanLink.replace("clan://localhost/", Init.getServerAddress(true) + "file/");
            } else {
                String link = lanLink.substring(7);
                int end = link.indexOf('/');
                return "http://" + link.substring(0, end) + "/file/" + link.substring(end + 1);
            }
        } else if (lanLink.startsWith("http://") && lanLink.contains("127.0.0.1")) {
            // 本机地址：指向自家 server 的 /file/ 文件地址（历史配置可能固化任意端口和目录段），一律重写到当前端口
            if (lanLink.contains("/file/")) {
                return lanLink.replaceFirst("http://127\\.0\\.0\\.1:\\d+/", Init.getServerAddress(true));
            }
            return lanLink.replaceAll("9978", Init.getServerPort() + "");
        } else if (lanLink.startsWith("file://")) {
            return lanLink.replace("file://", Init.getServerAddress(true) + "file/");
        } else {
            return lanLink;
        }
    }
}
