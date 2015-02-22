# A purely functional approach to building large applications

## Introduction

 * Who I am
 * Discuss prerequisites: Working of knowledge of Scala, this talk will be using Scalaz, monads/for-comprehensions
 * What we're going to do today


## Step 1 - First attempt

 * Introduce the problem. (Perhaps that sequence diagram?)
 * Present a simple, "naïve" solution:
   - A function, from twitter username to randomly generated text, `String` -> `Future[String]`
 * How will this be tested? Integratopn test?
 * Discuss configuration
 
## Step 2 - Configuration extraction, introducing ReaderT

 * Introduce the reader monad:
   - Talk about how functions are functors (compose)
   - Then talk about how functions are monads
   - Do all this with something mathsy, like `2 + _` (ie, `Int -> Int`)
   - Then change to be more concrete, ie (`Config -> Something`)
   - Gotta say something about Kleisli
   - Show how `Reader` works, `Reader(f)`
   - Do something like: `Config => String` for a couple, and use a flatMap on that.
   - Why are we doing this? So that we can provide different configurations for different environments.
   - Introduce monad transformers, `ReaderT`. Now we have an abstraction over the `Future`, we can rewrite our first attempt using `ReaderT`s
   
## Step 3 - Testing

 * Write a curried function, the first param list are parameters of functions returning `ReaderT[Function, Config, X]`s. Second is the parameter to the overall function. Returns a `ReaderT[Function, Config, Y]`.
   - We now have a way to test the way we wire the code together, separate from any calls to any third parties
   - Allude to this being "dependency injection"
 * Introduce Scalacheck, to "mock out" the call to the twitter service. Highlight that the markov generator has random embedded in it. This is the API we're using and we can't change it.  

## Step 4 - Abstraction

 * Abstract over the `Future`
   - Introduce the `Id` monad, so we don't need to have any code for waiting in our tests. Point out that the actual code in the wiring doesn't change!

## Step 4 - Enhancement

 * Introduce a logging function. Returns `Future[Unit]`
 * Change the test to include the logging.
 * Have the test use the `State` monad (or maybe the writer monad).
 
## Conclusion

 * Explain how the wiring method we've focused on is the same code that's run:
   - For unit testing, integration testing or production, different dependencies are wired in
   - For environments, different configurations are provided 
 * Highlight how we've used some powerful abstraction techniques to provide simple, clean, testable code
 * We now have this tidy, functional code that is built up using monadic composition and is executed on submission of a configuration
   