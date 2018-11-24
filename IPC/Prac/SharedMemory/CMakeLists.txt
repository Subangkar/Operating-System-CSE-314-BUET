cmake_minimum_required(VERSION 3.12)
project(SharedMem)

set(CMAKE_CXX_STANDARD 17)

add_executable(SharedMemHost host.cpp SharedMem.h)
add_executable(SharedMemClient client.cpp SharedMem.h)