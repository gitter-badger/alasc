package net.alasc.perms
package internal

import scala.util.hashing.MurmurHash3.unorderedHash

import spire.syntax.action._
import spire.syntax.cfor._

import net.alasc.syntax.permutationAction._
import net.alasc.algebra.PermutationAction

/** Permutation hash strategy with optimization for tiny (support in 0..15), small (support in 0..31) permutations.
  * 
  * - for permutations with supportMax <= 15, the hash function is described by `hash16`,
  * - for permutations with 16 <= supportMax <= 31, the hash function is described by `hash32`,
  * - for other permutations, the hash function is described by `hashLarge`.
  */
/* an
 * - for Perm16, the underlying Long bitstring is hashed using the standard Long hash (which xors both Int halves),
 * - for Perm32, scala.util.MurmurHash3.orderedHash is used with the permutation seed `Perm.seed`. supportMax + 1is used as the length,
 * - for PermArray, scala.util.MurmurHash3.unorderedHash is used with the (preimage, image) pairs that are in the support, i.e. only
 *   pairs with preimage != image are hashed. The (preimage, image) pair hashes are computed as (preimage ^ (image rotated 16 bits).
 */

object PermHash {
  @inline def seed = 0xf444c3b3
  import java.lang.Integer.rotateRight

  @inline final def pairHash(preimage: Int, image: Int) = preimage ^ rotateRight(image, 16)

  def hash16[P: PermutationAction](p: P): Int = {
    var encoding0to7 = 0
    var encoding8to15 = 0
    cforRange(0 until 7) { i =>
      encoding0to7 += (((i <|+| p) - i) & 0xF) << (i * 4)
    }
    cforRange(8 until 15) { i =>
      encoding8to15 += (((i <|+| p) - i) & 0xF) << (i * 4)
    }
    encoding0to7 ^ encoding8to15
  }

  def hash32[P: PermutationAction](p: P): Int = {
    @inline def encode(start: Int, length: Int = 6) = {
      var res = 0
      cforRange(start until start + length) { i =>
        res += (((i <|+| p) - i) & 0x1F) << ((i - start) * 5)
      }
      res
    }
    import scala.util.hashing.MurmurHash3.{mix, finalizeHash}
    var h = seed
    h = mix(h, encode(0))
    h = mix(h, encode(6))
    h = mix(h, encode(12))
    h = mix(h, encode(18))
    h = mix(h, encode(24))
    h = mix(h, encode(30, 2))
    finalizeHash(h, 6)
  }

  def hash[P: PermutationAction](p: P): Int = {
    val sm = p.largestMovedPoint.getOrElseFast(-1)
    if (sm <= 15)
      hash16(p)
    else if (sm <= 31)
      hash32(p)
    else
      unorderedHash(p.movedPoints.toSeq.map(k => pairHash(k, k <|+| p)), seed)
  }
}
