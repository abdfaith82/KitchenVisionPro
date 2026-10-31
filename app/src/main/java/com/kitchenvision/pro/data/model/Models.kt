package com.kitchenvision.pro.data.model

import androidx.compose.ui.geometry.Offset
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class PerspectiveLine(
    val id: String = java.util.UUID.randomUUID().toString(),
    val start: Offset,
    val end: Offset,
    val isVanishing: Boolean = false
)

data class DesignElement(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ElementType,
    val assetName: String,
    val position: Offset,
    val width: Float,
    val height: Float,
    val rotation: Float = 0f,
    val scale: Float = 1f,
    val materialId: String? = null,
    val colorTint: Long? = null,
    val zIndex: Int = 0,
    val visible: Boolean = true,
    val locked: Boolean = false,
    val name: String = ""
)

enum class ElementType {
    CABINET_UPPER, CABINET_LOWER, APPLIANCE, DECOR, LIGHTING, COUNTERTOP, BACKSPLASH
}

data class Material(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val color: Long,
    val category: MaterialCategory,
    val textureAsset: String? = null
)

enum class MaterialCategory {
    WOOD, STONE, METAL, GLASS, PAINT, FABRIC
}

data class KitchenProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val backgroundImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lines: List<PerspectiveLine> = emptyList(),
    val elements: List<DesignElement> = emptyList(),
    val canvasWidth: Float = 1080f,
    val canvasHeight: Float = 1920f
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val backgroundImagePath: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val linesJson: String,
    val elementsJson: String,
    val canvasWidth: Float,
    val canvasHeight: Float
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLinesList(value: List<PerspectiveLine>): String = gson.toJson(value)

    @TypeConverter
    fun toLinesList(value: String): List<PerspectiveLine> {
        val type = object : TypeToken<List<PerspectiveLine>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromElementsList(value: List<DesignElement>): String = gson.toJson(value)

    @TypeConverter
    fun toElementsList(value: String): List<DesignElement> {
        val type = object : TypeToken<List<DesignElement>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
}

object ElementLibrary {
    data class Item(
        val assetName: String,
        val labelAr: String,
        val type: ElementType,
        val defaultW: Float,
        val defaultH: Float
    )

    val all = listOf(
        Item("cabinet_upper_white", "خزانة علوية بيضاء", ElementType.CABINET_UPPER, 200f, 140f),
        Item("cabinet_upper_gray", "خزانة علوية رمادية", ElementType.CABINET_UPPER, 200f, 140f),
        Item("cabinet_upper_wood", "خزانة علوية خشب", ElementType.CABINET_UPPER, 200f, 140f),
        Item("cabinet_upper_black", "خزانة علوية سوداء", ElementType.CABINET_UPPER, 200f, 140f),
        Item("cabinet_upper_glass", "خزانة علوية زجاج", ElementType.CABINET_UPPER, 200f, 140f),
        Item("cabinet_lower_wood", "خزانة سفلية خشب", ElementType.CABINET_LOWER, 200f, 170f),
        Item("cabinet_lower_white", "خزانة سفلية بيضاء", ElementType.CABINET_LOWER, 200f, 170f),
        Item("cabinet_lower_gray", "خزانة سفلية رمادية", ElementType.CABINET_LOWER, 200f, 170f),
        Item("cabinet_lower_black", "خزانة سفلية سوداء", ElementType.CABINET_LOWER, 200f, 170f),
        Item("oven_black", "فرن أسود", ElementType.APPLIANCE, 160f, 160f),
        Item("oven_stainless", "فرن ستانلس", ElementType.APPLIANCE, 160f, 160f),
        Item("cooktop", "موقد", ElementType.APPLIANCE, 200f, 110f),
        Item("fridge_side", "ثلاجة", ElementType.APPLIANCE, 130f, 200f),
        Item("dishwasher", "غسالة صحون", ElementType.APPLIANCE, 160f, 140f),
        Item("range_hood", "شفاط", ElementType.APPLIANCE, 180f, 80f),
        Item("microwave", "مايكروويف", ElementType.APPLIANCE, 140f, 100f),
        Item("plant_01", "نبات 1", ElementType.DECOR, 90f, 110f),
        Item("plant_02", "نبات 2", ElementType.DECOR, 90f, 110f),
        Item("pot_black", "قدر", ElementType.DECOR, 80f, 70f),
        Item("fruit_bowl", "وعاء فاكهة", ElementType.DECOR, 100f, 65f),
        Item("led_strip", "شريط LED", ElementType.LIGHTING, 180f, 28f),
        Item("under_cabinet_light", "إضاءة تحت الخزانة", ElementType.LIGHTING, 180f, 28f),
        Item("countertop_marble", "سطح رخام", ElementType.COUNTERTOP, 280f, 36f),
        Item("countertop_wood", "سطح خشب", ElementType.COUNTERTOP, 280f, 36f),
        Item("countertop_black", "سطح أسود", ElementType.COUNTERTOP, 280f, 36f)
    )

    fun byType(type: ElementType) = all.filter { it.type == type }
    fun find(assetName: String) = all.find { it.assetName == assetName }
}

object MaterialLibrary {
    val all = listOf(
        Material("wood_oak", "خشب بلوط", "Oak Wood", 0xFFD4A574, MaterialCategory.WOOD),
        Material("wood_walnut", "خشب جوز", "Walnut", 0xFF5C4033, MaterialCategory.WOOD),
        Material("wood_light", "خشب فاتح", "Light Wood", 0xFFE8D5B7, MaterialCategory.WOOD),
        Material("marble_white", "رخام أبيض", "White Marble", 0xFFF5F5F5, MaterialCategory.STONE),
        Material("marble_black", "رخام أسود", "Black Marble", 0xFF2C2C2C, MaterialCategory.STONE),
        Material("marble_gray", "رخام رمادي", "Gray Marble", 0xFF8A8A8A, MaterialCategory.STONE),
        Material("granite_dark", "جرانيت داكن", "Dark Granite", 0xFF3D3D3D, MaterialCategory.STONE),
        Material("metal_steel", "ستانلس ستيل", "Stainless Steel", 0xFFC0C0C0, MaterialCategory.MET
