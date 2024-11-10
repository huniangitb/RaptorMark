cmake_minimum_required(VERSION 3.16)

file(GLOB_RECURSE CORE_LATENCY_SOURCES "*.c")

add_library(core_latency SHARED ${CORE_LATENCY_SOURCES})
target_sources(core_latency INTERFACE ${CORE_LATENCY_SOURCES})

target_include_directories(core_latency PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})