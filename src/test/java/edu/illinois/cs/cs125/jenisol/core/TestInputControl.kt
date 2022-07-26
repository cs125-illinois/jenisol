package edu.illinois.cs.cs125.jenisol.core

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.Scanner

class TestInputControl : StringSpec({
    "it should control input" {
        defaultControlInput {
            open("Here")
            val inputScanner = Scanner(System.`in`)
            inputScanner.nextLine() shouldBe "Here"
        }
    }
    "it should reopen input" {
        defaultControlInput {
            open("First")
            val firstScanner = Scanner(System.`in`)
            firstScanner.nextLine() shouldBe "First"

            open("Second")
            val secondScanner = Scanner(System.`in`)
            secondScanner.nextLine() shouldBe "Second"
        }
    }
})