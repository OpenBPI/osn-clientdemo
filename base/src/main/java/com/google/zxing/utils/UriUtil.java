package com.google.zxing.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * Uri路径工具
 */

public class UriUtil {
    /**
     * 根据Uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     * getRealPathFromUri
     */
    //其他手机url路径：content://media/external/images/media/299
    //华为手机获取的路径：content://com.android.providers.media.documents/document/image%3A100595
    //intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    public static String getRealPathFromUri(Context context, Uri uri) {
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion >= 19) { // api >= 19
            return getRealPathFromUriAboveApi19(context, uri);
        } else { // api < 19
            return getRealPathFromUriBelowAPI19(context, uri);
        }
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
        return getDataColumn(context, uri, null, null);
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
//    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            String documentId = DocumentsContract.getDocumentId(uri);
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                String id = documentId.split(":")[1];

                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = {id};
                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
                filePath = getDataColumn(context, contentUri, null, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null);
        } else if ("file".equals(uri.getScheme())) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.getPath();
        }
        return filePath;
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        String path = null;

        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
                path = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
}

//    /**
//     * 根据图片的Uri获取图片的绝对路径(适配多种API)
//     *
//     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
//     */
//    public static String getRealPathFromUri(Context context, Uri uri) {
//        int sdkVersion = Build.VERSION.SDK_INT;
//        if (sdkVersion < 11) return getRealPathFromUri_BelowApi11(context, uri);
//        if (sdkVersion < 19) return getRealPathFromUri_Api11To18(context, uri);
//        else return getRealPathFromUriAboveApi19(context, uri);//getRealPathFromUri_AboveApi19
//       // getRealPathFromUriAboveApi19
//    }
//
//    /**
//     * 适配api19以上,根据uri获取图片的绝对路径
//     */
//    @TargetApi(Build.VERSION_CODES.KITKAT)
////    private static String getRealPathFromUri_AboveApi19(Context context, Uri uri) {
////        String filePath = null;
////        String wholeID = DocumentsContract.getDocumentId(uri);
////
////        // 使用':'分割
////        String[] ids = wholeID.split(":");
////        String id = null;
////        if (ids == null) {
////            return null;
////        }
////        if (ids.length > 1) {
////            id = ids[1];
////        } else {
////            id = ids[0];
////        }
////
////        String[] projection = {MediaStore.Images.Media.DATA};
////        String selection = MediaStore.Images.Media._ID + "=?";
////        String[] selectionArgs = {id};
////
////        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,//
////                projection, selection, selectionArgs, null);
////        int columnIndex = cursor.getColumnIndex(projection[0]);
////        if (cursor.moveToFirst()) filePath = cursor.getString(columnIndex);
////        cursor.close();
////        return filePath;
////    }
//    private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
//        String filePath = null;
//        if (DocumentsContract.isDocumentUri(context, uri)) {
//            // 如果是document类型的 uri, 则通过document id来进行处理
//            String documentId = DocumentsContract.getDocumentId(uri);
//            if (isMediaDocument(uri)) { // MediaProvider
//                // 使用':'分割
//                String id = documentId.split(":")[1];
//
//                String selection = MediaStore.Images.Media._ID + "=?";
//                String[] selectionArgs = {id};
//                filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
//            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
//                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
//                filePath = getDataColumn(context, contentUri, null, null);
//            }
//        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            // 如果是 content 类型的 Uri
//            filePath = getDataColumn(context, uri, null, null);
//        } else if ("file".equals(uri.getScheme())) {
//            // 如果是 file 类型的 Uri,直接获取图片对应的路径
//            filePath = uri.getPath();
//        }
//        return filePath;
//    }
//
//    /**
//     * 适配api11-api18,根据uri获取图片的绝对路径
//     */
//    private static String getRealPathFromUri_Api11To18(Context context, Uri uri) {
//        String filePath = null;
//        String[] projection = {MediaStore.Images.Media.DATA};
//        CursorLoader loader = new CursorLoader(context, uri, projection, null, null, null);
//        Cursor cursor = loader.loadInBackground();
//
//        if (cursor != null) {
//            cursor.moveToFirst();
//            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
//            cursor.close();
//        }
//        return filePath;
//    }
//
//    /**
//     * 适配api11以下(不包括api11),根据uri获取图片的绝对路径
//     */
//    private static String getRealPathFromUri_BelowApi11(Context context, Uri uri) {
//        String filePath = null;
//        String[] projection = {MediaStore.Images.Media.DATA};
//        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
//        if (cursor != null) {
//            cursor.moveToFirst();
//            filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
//            cursor.close();
//        }
//        return filePath;
//    }
//
//    /**
//     * @param uri the Uri to check
//     * @return Whether the Uri authority is MediaProvider
//     */
//    private static boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * @param uri the Uri to check
//     * @return Whether the Uri authority is DownloadsProvider
//     */
//    private static boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
//    }
//
//    /**
//     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
//     *
//     * @param context 上下文对象
//     * @param uri     图片的Uri
//     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
//     */
//    private static String getRealPathFromUriBelowAPI19(Context context, Uri uri) {
//        return getDataColumn(context, uri, null, null);
//    }
//
//    /**
//     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
//     *
//     * @return
//     */
//    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
//        String path = null;
//
//        String[] projection = new String[]{MediaStore.Images.Media.DATA};
//        Cursor cursor = null;
//        try {
//            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
//                path = cursor.getString(columnIndex);
//            }
//        } catch (Exception e) {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//        return path;
//    }
//}
