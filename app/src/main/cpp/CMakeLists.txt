cmake_minimum_required(VERSION 3.16)
project("raptormark")

add_subdirectory(cjson)
add_subdirectory(libaio)
add_subdirectory(fio)

add_library(raptormark-jni SHARED raptormark-jni.cpp helper.cpp)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=gnu99")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

target_link_libraries(raptormark-jni cjson log)

# fio_runner: standalone executable for running fio with root privileges
# Links against the same fio library and cJSON for JSON config parsing
add_executable(fio_runner fio_runner.c)
target_include_directories(fio_runner PRIVATE
    ${CMAKE_SOURCE_DIR}/cjson
)
target_link_libraries(fio_runner cjson dl)
target_compile_options(fio_runner PRIVATE -std=gnu11)

# Place in same output dir as shared libs for packaging
set_target_properties(fio_runner PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}"
)

# Post-build: copy fio_runner to a location accessible for asset packaging
add_custom_command(TARGET fio_runner POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy
        "$<TARGET_FILE:fio_runner>"
        "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/fio_runner"
    COMMENT "Copying fio_runner for asset packaging"
)
