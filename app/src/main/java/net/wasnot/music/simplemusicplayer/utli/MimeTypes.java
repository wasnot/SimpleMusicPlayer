
package net.wasnot.music.simplemusicplayer.utli;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.webkit.MimeTypeMap;

/**
 * android標準クラスを使っている でもあまり精度が高くない気がするので、contentproviderから読んでいるものはそのまま使っている。
 * 
 * @author akihiroaida
 */
public class MimeTypes {
    // ファイル名からMIMEタイプ取得
    public static String GetMIMEType(String Path) {
        // 拡張子を取得
        // String ext=MimeTypeMap.getFileExtensionFromUrl(Path);
        // //小文字に変換
        // ext=ext.toLowerCase();
        if (Path == null) {
            return null;
        }
        // ファイルから拡張子取得
        File file = new File(Path);
        String fn = file.getName();
        int ch = fn.lastIndexOf('.');
        String ext = (ch >= 0) ? fn.substring(ch + 1) : null;

        // 拡張子からMIMEType取得
        if (ext != null) {
            String MIME = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());
            // LogUtil.v("mimetype",ext+"  :  "+MIME);
            if (MIME != null) {
                return MIME;
            }
        }
        // 拡張子がついていないか判別できないときはbinary
        return "application/octet-stream";
    }

    /**
     * pathのディレクトリ内にあるextensionの拡張子ファイルを再帰的に取得する
     * 
     * @param path
     * @param ext
     * @return
     */
    private static String[] LoadPathFiles(String path, final String[] ext) {
        List<String> list = new ArrayList<String>();

        File dir = new File(path);
        if (dir == null) {
            LogUtil.d("SelectPicture:LoadPathPicture",
                    "It can not exist Directory selected path. path:" + path);
            return null;
        }

        File[] files = dir.listFiles(getFileExtension(ext));

        if (files != null && files.length > 0) {
            // ファイルが存在していた時のみ処理を行う
            for (int n = 0; n < files.length; n++) {

                if (files[n].isDirectory()) {
                    // ディレクトリの場合再帰的に検索する
                    String[] ret = LoadPathFiles(files[n].getPath(), ext);
                    // /*
                    if (ret != null) {
                        for (int na = 0; na < ret.length; na++)
                            list.add(ret[na].toString());
                    }
                    // */
                    continue;
                }
                list.add(files[n].getPath());
            }
        }
        String[] res = list.toArray(new String[list.size()]);
        return res;
    }

    private static FilenameFilter getFileExtension(String[] exts) {
        final String[] extensions = exts;
        return new FilenameFilter() {
            public boolean accept(File file, String name) {
                // ディレクトリ判別
                File tmp = new File(file.getPath() + "/" + name);
                if (tmp.isDirectory()) {
                    return true;
                }
                // 拡張子判別
                /*
                 * if( name.endsWith("jpeg") || name.endsWith("jpg") ) return
                 * true; return false;
                 */
                // /*
                for (int n = 0; n < extensions.length; n++) {
                    if (name.endsWith(extensions[n]))
                        return true;
                }
                return false;
                // */
            }
        };
    }
}
