package com.github.secretx33.magicwands.repositories

import com.github.secretx33.magicwands.database.SQLite
import com.github.secretx33.magicwands.model.SpellTeacher
import com.github.secretx33.magicwands.model.SpellType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Location
import org.bukkit.block.Block
import java.util.concurrent.ConcurrentHashMap

class SpellTeacherRepo(private val db: SQLite) {

    private val spellTeachers = ConcurrentHashMap<Location, SpellTeacher>()

    init { loadDataFromDB() }

    private fun loadDataFromDB() = CoroutineScope(Dispatchers.Default).launch {
        spellTeachers.clear()
        spellTeachers.putAll(db.getAllSpellteachersAsync().await())
    }

    fun isSpellTeacher(block: Block): Boolean = spellTeachers.containsKey(block.location)

    fun getTeacherType(block: Block): SpellType? = spellTeachers[block.location]?.spellType /*?: throw NoSuchElementException("${ChatColor.RED}Block at ${block.location} is not a SpellTeacher!")*/

    fun makeSpellTeacher(block: Block, spell: SpellType) {
        if(block.location.world == null) return

        val teacher = block.asSpellTeacher(spell)
        spellTeachers[teacher.location] = teacher
        db.addSpellteacher(teacher)
    }

    fun updateSpellTeacher(block: Block, spell: SpellType) {
        if(block.location.world == null) return
        val newTeacher = block.asSpellTeacher(spell)
        spellTeachers[block.location] = newTeacher
        db.updateSpellteacher(newTeacher)
    }

    fun removeSpellTeacher(block: Block) {
        if(block.location.world == null) return

        spellTeachers.remove(block.location)
        db.removeSpellteacher(block.location)
    }

    private fun Block.asSpellTeacher(spellType: SpellType) = SpellTeacher(location, spellType, type)
}
