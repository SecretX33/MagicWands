package com.github.secretx33.magicwands.packets

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.reflect.IntEnum
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import java.util.*

class WrapperPlayServerSpawnEntity : AbstractPacket {
    /**
     * Represents the different object types.
     *
     * @author Kristian
     */
    object ObjectTypes : IntEnum() {
        const val BOAT = 1
        const val ITEM_STACK = 2
        const val AREA_EFFECT_CLOUD = 3
        const val MINECART = 10
        const val ACTIVATED_TNT = 50
        const val ENDER_CRYSTAL = 51
        const val TIPPED_ARROW_PROJECTILE = 60
        const val SNOWBALL_PROJECTILE = 61
        const val EGG_PROJECTILE = 62
        const val GHAST_FIREBALL = 63
        const val BLAZE_FIREBALL = 64
        const val THROWN_ENDERPEARL = 65
        const val WITHER_SKULL_PROJECTILE = 66
        const val SHULKER_BULLET = 67
        const val FALLING_BLOCK = 70
        const val ITEM_FRAME = 71
        const val EYE_OF_ENDER = 72
        const val THROWN_POTION = 73
        const val THROWN_EXP_BOTTLE = 75
        const val FIREWORK_ROCKET = 76
        const val LEASH_KNOT = 77
        const val ARMORSTAND = 78
        const val FISHING_FLOAT = 90
        const val SPECTRAL_ARROW = 91
        const val DRAGON_FIREBALL = 93
    }

    constructor() : super(PacketContainer(TYPE), TYPE) {
        handle.modifier.writeDefaults()
    }

    constructor(packet: PacketContainer) : super(packet, TYPE) {}

    var entityID: Int
        /**
         * Retrieve entity ID of the Object.
         *
         * @return The current EID
         */
        get() = handle.integers.read(0)
        /**
         * Set entity ID of the Object.
         *
         * @param value - new value.
         */
        set(value) {
            handle.integers.write(0, value)
        }

    /**
     * Retrieve the entity that will be spawned.
     *
     * @param world - the current world of the entity.
     * @return The spawned entity.
     */
    fun getEntity(world: World): Entity
        = handle.getEntityModifier(world).read(0)

    /**
     * Retrieve the entity that will be spawned.
     *
     * @param event - the packet event.
     * @return The spawned entity.
     */
    fun getEntity(event: PacketEvent): Entity = getEntity(event.player.world)

    var uniqueId: UUID
        get() = handle.uuiDs.read(0)
        set(value) {
            handle.uuiDs.write(0, value)
        }

    var x: Double
        /**
         * Retrieve the x position of the object.
         *
         * Note that the coordinate is rounded off to the nearest 1/32 of a meter.
         *
         * @return The current X
         */
        get() = handle.doubles.read(0)
        /**
         * Set the x position of the object.
         *
         * @param value - new value.
         */
        set(value) {
            handle.doubles.write(0, value)
        }

    var y: Double
        /**
         * Retrieve the y position of the object.
         *
         * Note that the coordinate is rounded off to the nearest 1/32 of a meter.
         *
         * @return The current y
         */
        get() = handle.doubles.read(1)
        /**
         * Set the y position of the object.
         *
         * @param value - new value.
         */
        set(value) {
            handle.doubles.write(1, value)
        }

    var z: Double
        /**
         * Retrieve the z position of the object.
         *
         *
         * Note that the coordinate is rounded off to the nearest 1/32 of a meter.
         *
         * @return The current z
         */
        get() = handle.doubles.read(2)
        /**
         * Set the z position of the object.
         *
         * @param value - new value.
         */
        set(value) {
            handle.doubles.write(2, value)
        }

    var optionalSpeedX: Double
        /**
         * Retrieve the optional speed x.
         *
         *
         * This is ignored if [.getObjectData] is zero.
         *
         * @return The optional speed x.
         */
        get() = handle.integers.read(1) / 8000.0
        /**
         * Set the optional speed x.
         *
         * @param value - new value.
         */
        set(value) {
            handle.integers.write(1, (value * 8000.0).toInt())
        }


    var optionalSpeedY: Double
        /**
         * Retrieve the optional speed y.
         *
         * This is ignored if [.getObjectData] is zero.
         *
         * @return The optional speed y.
         */
        get() = handle.integers.read(2) / 8000.0
        /**
         * Set the optional speed y.
         *
         * @param value - new value.
         */
        set(value) {
            handle.integers.write(2, (value * 8000.0).toInt())
        }


    var optionalSpeedZ: Double
        /**
         * Retrieve the optional speed z.
         *
         *
         * This is ignored if [.getObjectData] is zero.
         *
         * @return The optional speed z.
         */
        get() = handle.integers.read(3) / 8000.0
        /**
         * Set the optional speed z.
         *
         * @param value - new value.
         */
        set(value) {
            handle.integers.write(3, (value * 8000.0).toInt())
        }
    
    var pitch: Float
        /**
         * Retrieve the pitch.
         *
         * @return The current pitch.
         */
        get() = handle.integers.read(4) * 360f / 256.0f
        /**
         * Set the pitch.
         *
         * @param value - new pitch.
         */
        set(value) {
            handle.integers.write(4, (value * 256.0f / 360.0f).toInt())
        }

    var yaw: Float
        /**
         * Retrieve the yaw.
         *
         * @return The current Yaw
         */
        get() = handle.integers.read(5) * 360f / 256.0f
        /**
         * Set the yaw of the object spawned.
         *
         * @param value - new yaw.
         */
        set(value) {
            handle.integers.write(5, (value * 256.0f / 360.0f).toInt())
        }

    var type: EntityType
        /**
         * Retrieve the type of object. See [ObjectTypes]
         *
         * @return The current Type
         */
        get() = handle.entityTypeModifier.read(0)
        /**
         * Set the type of object. See [ObjectTypes].
         *
         * @param value - new value.
         */
        set(value) {
            handle.entityTypeModifier.write(0, value)
        }

    var objectData: Int
        /**
         * Retrieve object data.
         *
         *
         * The content depends on the object type:
         * <table border="1" cellpadding="4">
         * <tr>
         * <th>Object Type:</th>
         * <th>Name:</th>
         * <th>Description</th>
        </tr> *
         * <tr>
         * <td>ITEM_FRAME</td>
         * <td>Orientation</td>
         * <td>0-3: South, West, North, East</td>
        </tr> *
         * <tr>
         * <td>FALLING_BLOCK</td>
         * <td>Block Type</td>
         * <td>BlockID | (Metadata << 0xC)</td>
        </tr> *
         * <tr>
         * <td>Projectiles</td>
         * <td>Entity ID</td>
         * <td>The entity ID of the thrower</td>
        </tr> *
         * <tr>
         * <td>Splash Potions</td>
         * <td>Data Value</td>
         * <td>Potion data value.</td>
        </tr> *
        </table> *
         *
         * @return The current object Data
         */
        get() = handle.integers.read(6)
        /**
         * Set object Data.
         *
         *
         * The content depends on the object type. See [.getObjectData] for
         * more information.
         *
         * @param value - new object data.
         */
        set(value) {
            handle.integers.write(6, value)
        }

    companion object {
        val TYPE: PacketType = PacketType.Play.Server.SPAWN_ENTITY
    }
}