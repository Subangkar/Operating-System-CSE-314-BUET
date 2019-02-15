cmake_minimum_required(VERSION 3.12)
project(CakeFactory)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -pthread -w")

add_executable(CakeFactory CakeFactory.cpp)
add_executable(SemaphoreTest semaphore.cpp)
add_executable(ThreadTest simple_thread.cpp)
add_executable(ProdConsTest prod_cons_without_mutex.cpp)
add_executable(ProdConsMutexTest prod_cons_with_mutex.cpp)