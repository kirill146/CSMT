import CSMT
import junit.framework.TestCase
import org.junit.Test
import java.util.*
import kotlin.math.absoluteValue

val rnd = Random()

fun rand() = 1 + rnd.nextInt().absoluteValue % 1000

fun CSMT.insert(x: Int) = this.insert(x, x.toString())

fun CSMT.present(x: Int) = this.getData(x) != null

data class Testset(val inserts: List<Int>, val erases: List<Int>)

fun GenInserts(n: Int) : List<Int> {
    val inserts = mutableSetOf<Int>()
    repeat(n) {
        inserts.add(rand())
    }
    return inserts.toList()
}

fun GenTestset(): Testset {
    val inserts = GenInserts(100)
    val erases = mutableSetOf<Int>()

    repeat(30) {
        var x = 1
        while (x !in inserts) {
            x = rand()
        }
        erases.add(x)
    }
    return Testset(inserts.toList(), erases.toList())
}

class Tests : TestCase() {

    fun testHistoryIndependence(withErase: Boolean) {
        repeat(1_000) { _ ->
            val (inserts, erases) = GenTestset()

            val insertTestset1 = inserts
            val insertTestset2 = insertTestset1.shuffled(rnd)

            val eraseTestset1 = erases
            val eraseTestset2 = eraseTestset1.shuffled(rnd)

            val tree1 = CSMT()
            val tree2 = CSMT()

            insertTestset1.forEach { tree1.insert(it) }
            if (withErase) eraseTestset1.forEach(tree1::delete)

            insertTestset2.forEach { tree2.insert(it) }
            if (withErase) eraseTestset2.forEach(tree2::delete)

            assert(tree1.rootHash!!.contentEquals(tree2.rootHash))
        }
    }

    fun testAllPresent(withErase: Boolean) {
        repeat(1_000) { _ ->
            val (inserts, erases) = GenTestset()

            val tree = CSMT()
            inserts.forEach { tree.insert(it) }
            if (withErase) erases.forEach { tree.delete(it) }

            val inTree = if (withErase) inserts.filterNot { it in erases } else inserts

            assert(inTree.all(tree::present))
        }
    }

    @Test
    fun testHistoryIndependenceInsert() {
        testHistoryIndependence(withErase = false)
    }

    @Test
    fun testHistoryIndependenceInsertErase() {
        testHistoryIndependence(withErase = true)
    }

    @Test
    fun testAllPresentInsert() {
        testAllPresent(withErase = false)
    }

    @Test
    fun testAllPresentInsertErase() {
        testAllPresent(withErase = true)
    }

    @Test
    fun testNotPresentExtras() {
        repeat(1_000) { _ ->
            val inserts = GenInserts(500)

            val tree = CSMT()
            inserts.forEach { tree.insert(it) }

            repeat(10_000) {
                assert(tree.present(it) == it in inserts)
            }
        }
    }

    @Test
    fun testGetData() {
        repeat(1_000) { _ ->
            val inserts = GenInserts(500)
            //val inserts = GenInserts(100)

            val tree = CSMT()
            inserts.forEach { tree.insert(it) }

            assert(inserts.all { tree.getData(it) == it.toString() })
        }
    }

    @Test
    fun testCorrectMembershipProof() {
        repeat(1_000) { _ ->
            val inserts = GenInserts(500)

            val tree = CSMT()
            inserts.forEach { tree.insert(it) }

            repeat(10_000) {
                when (it) {
                    in inserts -> {
                        val ok = CSMT.verifyMembershipProof(
                                tree.rootHash,
                                it,
                                tree.getData(it),
                                tree.getProof(it)[0]
                        )
                        //println("ok ($it) : $ok | data = ${tree.getData(it)}")
                        assert(ok)
                    }
                    !in inserts -> {
                        val proof = tree.getProof(it)
                        val data1 = proof[0]?.key?.let(tree::getData)
                        val data2 = proof[1]?.key?.let(tree::getData)
                        assert(CSMT.verifyNonMembershipProof(
                                tree.rootHash,
                                it,
                                data1,
                                data2,
                                proof
                        ))
                    }
                }
            }
        }
    }

}