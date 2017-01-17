package org.slug.util

interface Either<out L, out R>
data class Left<out T>(val value: T) : Either<T, Nothing>
data class Right<out T>(val value: T) : Either<Nothing, T>

inline fun <L, R, T> Either<L, R>.mapLeft(left: (L) -> T): Either<T, R> =
    when (this) {
      is Left -> Left(left(value))
      is Right -> Right(value)
      else -> throw IllegalArgumentException("we should never be here, this is a language quirk")
    }

fun <L, R, T> Either<L, R>.mapRight(right: (R) -> T): Either<L, T> =
    when (this) {
      is Left -> Left(value)
      is Right -> Right(right(value))
      else -> throw IllegalArgumentException("we should never be here, this is a language quirk")
    }

fun <L, R, T> Either<L, R>.fold(left: (L) -> T, right: (R) -> T): T =
    when (this) {
      is Left -> left(value)
      is Right -> right(value)
      else -> throw IllegalArgumentException("we should never be here, this is a language quirk")
    }

fun <L, R> Either<L, R>.isLeft(): Boolean {
  when (this) {
    is Left -> return true
    is Right -> return false
    else -> throw IllegalArgumentException("we should never be here, this is a language quirk")
  }
}

fun <L, R> Either<L, R>.isRight(): Boolean {
  when (this) {
    is Left -> return false
    is Right -> return true
    else -> throw IllegalArgumentException("we should never be here, this is a language quirk")
  }
}

fun <L, R> Either<L, R>.leftValue(): L {
  when (this) {
    is Left -> return value
    is Right -> throw Exception("We got a right value in left!!")
    else -> throw IllegalArgumentException("we should never be here, this is a language quirk")
  }
}

fun <L, R> Either<L, R>.rightValue(): R {
  when (this) {
    is Left -> throw Exception("We got a left value in right!!")
    is Right -> return value
    else -> throw IllegalArgumentException("we should never be here, this is a language quirk")
  }
}