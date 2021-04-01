package com.github.secretx33.magicwands.packets

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import org.bukkit.Location

class WrapperPlayServerWorldParticles : AbstractPacket {
    
    constructor() : super(PacketContainer(TYPE), TYPE) {
        handle.modifier.writeDefaults()
    }

    constructor(packet: PacketContainer) : super(packet, TYPE) {}

    /**
     * Retrieve or set the particle type.
     *
     * @return The current particle
     */
    var particleType: EnumWrappers.Particle
        get() = handle.particles.read(0)
        set(value) {
            handle.particles.write(0, value)
        }

    /**
     * Retrieves or set X.
     *
     *
     * Notes: x position of the particle
     *
     * @return The current X
     */
    var x: Double
        get() = handle.doubles.read(0)
        set(value) {
            handle.doubles.write(0, value)
        }

    /**
     * Retrieve or set Y.
     *
     * Notes: y position of the particle
     *
     * @return The current Y
     */
    var y: Double
        get() = handle.doubles.read(1)
        set(value) {
            handle.doubles.write(1, value)
        }

    /**
     * Retrieve or set Z.
     *
     *
     * Notes: z position of the particle
     *
     * @return The current Z
     */
    var z: Double
        get() = handle.doubles.read(2)
        set(value) {
            handle.doubles.write(2, value)
        }

    /**
     * Retrieve or set particle Location.
     *
     * Notes: It's a convenience method for easy setting
     * x, y and z.
     *
     * @return A new location containing current X, Y and Z
     */
    var location: Location
        get() = Location(null, x, y, z)
        set(value) {
            x = value.x
            y = value.y
            z = value.z
        }

    /**
     * Retrieve or set Offset X.
     *
     *
     * Notes: this is added to the X position after being multiplied by
     * random.nextGaussian()
     *
     * @return The current Offset X
     */
    var offsetX: Float
        get() = handle.float.read(0)
        set(value) {
            handle.float.write(0, value)
        }

    /**
     * Retrieve or set Offset Y.
     *
     *
     * Notes: this is added to the Y position after being multiplied by
     * random.nextGaussian()
     *
     * @return The current Offset Y
     */
    var offsetY: Float
        get() = handle.float.read(1)
        set(value) {
            handle.float.write(1, value)
        }

    /**
     * Retrieve or set Offset Z.
     *
     *
     * Notes: this is added to the Z position after being multiplied by
     * random.nextGaussian()
     *
     * @return The current Offset Z
     */
    var offsetZ: Float
        get() = handle.float.read(2)
        set(value) {
            handle.float.write(2, value)
        }
    
    /**
     * Retrieve or set Particle data.
     *
     * Notes: the data of each particle
     *
     * @return The current Particle data
     */
    var particleData: Float
        get() = handle.float.read(3)
        set(value) {
            handle.float.write(3, value)
        }
    
    /**
     * Retrieve or set Number of particles.
     *
     * Notes: the number of particles to create
     *
     * @return The current Number of particles
     */
    var numberOfParticles: Int
        get() = handle.integers.read(0)
        set(value) {
            handle.integers.write(0, value)
        }
    
    /**
     * Enable or disable Long Distance mode.
     *
     * Notes: if true, particle distance increases from 256 to 65536.
     *
     * @return The current Long Distance
     */
    var longDistance: Boolean
        get() = handle.booleans.read(0)
        set(value) {
            handle.booleans.write(0, value)
        }

    companion object {
        val TYPE: PacketType = PacketType.Play.Server.WORLD_PARTICLES
    }
}