package com.kitchenvision.pro.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.kitchenvision.pro.data.model.DesignElement
import com.kitchenvision.pro.data.model.PerspectiveLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {

    suspend fun renderDesignToBitmap(
        context: Context,
        backgroundUri: Uri?,
        lines: List<PerspectiveLine>,
        elements: List<DesignElement>,
        canvasWidth: Int,
        canvasHeight: Int
    ): Bitmap = withContext(Dispatchers.Default) {

        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        if (backgroundUri != null) {
            try {
                context.contentResolver.openInputStream(backgroundUri)?.use { stream ->
                    val bg = BitmapFactory.decodeStream(stream)
                    if (bg != null) {
                        val scaled = Bitmap.createScaledBitmap(bg, canvasWidth, canvasHeight, true)
                        canvas.drawBitmap(scaled, 0f, 0f, null)
                        if (scaled != bg) scaled.recycle()
                        bg.recycle()
                    } else canvas.drawColor(Color.parseColor("#3A3A3A"))
                }
            } catch (_: Exception) {
                canvas.drawColor(Color.parseColor("#3A3A3A"))
            }
        } else {
            canvas.drawColor(Color.parseColor("#3A3A3A"))
        }

        // Perspective lines
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF5722")
            strokeWidth = 4f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(16f, 10f), 0f)
        }
        val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF5722")
            style = Paint.Style.FILL
        }
        lines.forEach { line ->
            canvas.drawLine(line.start.x, line.start.y, line.end.x, line.end.y, linePaint)
            canvas.drawCircle(line.start.x, line.start.y, 10f, pointPaint)
            canvas.drawCircle(line.end.x, line.end.y, 10f, pointPaint)
        }

        // Elements (already sorted by zIndex, only visible)
        elements.forEach { el ->
            val assetBmp = loadAssetBitmap(context, el.assetName)
            val left = el.position.x
            val top = el.position.y
            val right = left + el.width * el.scale
            val bottom = top + el.height * el.scale

            canvas.save()
            canvas.rotate(el.rotation, (left + right) / 2f, (top + bottom) / 2f)

            if (assetBmp != null) {
                val dest = RectF(left, top, right, bottom)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                // Optional tint from material
                el.materialId?.let { mid ->
                    // soft color overlay kept minimal for realism
                }
                canvas.drawBitmap(assetBmp, null, dest, paint)
                assetBmp.recycle()
            } else {
                // fallback rect
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E0C9A6")
                }
                canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, paint)
            }
            canvas.restore()
        }

        // Watermark
        val watermark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            alpha = 90
            textSize = 28f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("KitchenVision Pro", canvasWidth - 24f, canvasHeight - 24f, watermark)
        bitmap
    }

    private fun loadAssetBitmap(context: Context, assetName: String): Bitmap? {
        return try {
            context.assets.open("elements/$assetName.png").use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap, name: String = "KitchenDesign"): Uri? =
        withContext(Dispatchers.IO) {
            val filename = "${name}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KitchenVision")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                        }
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(it, values, null, null)
                    }
                    uri
                } else {
                    val dir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "KitchenVision"
                    )
                    if (!dir.exists()) dir.mkdirs()
                    val file = File(dir, filename)
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DATA, file.absolutePath)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    }
                    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    fun shareImage(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "تصميم مطبخ من KitchenVision Pro")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة التصميم"))
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
