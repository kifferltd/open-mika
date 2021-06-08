# Simple CMake wrapper for building Mika for the imsys target using the build-imsys.sh.in script
cmake_minimum_required(VERSION 3.16)

project(imsys-mika NONE)

if (NOT CMAKE_BUILD_TYPE)
  set (CMAKE_BUILD_TYPE Release)
endif ()

set (IMSYS_ISAL_PHASE 1 CACHE STRING "ISAL implementation phase")

set (IMSYS_MIKA_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR})
set (IMSYS_MIKA_BUILD_DIR ${CMAKE_CURRENT_BINARY_DIR})

if( NOT ${IMSYS_MIKA_BUILD_DIR} STREQUAL ${IMSYS_MIKA_SOURCE_DIR}/build)
  message( FATAL_ERROR "Mika build directory must be sw-open-mika/build" )
endif()

configure_file (${IMSYS_MIKA_SOURCE_DIR}/build-imsys.sh.in ${IMSYS_MIKA_BUILD_DIR}/build-imsys.sh @ONLY)

set (IMSYS_MIKA_INCLUDE_DIR ${IMSYS_MIKA_SOURCE_DIR}/core-vm/include)
set (IMSYS_MIKA_LIB_DIR ${IMSYS_MIKA_BUILD_DIR}/im4000/lib)
set (IMSYS_MIKA_OSWALD_FILE ${IMSYS_MIKA_LIB_DIR}/liboswald.a)

# Dummy output which is never actually produced. Anything that depends on
# this will always be rebuilt.
add_custom_command(
    OUTPUT always_rebuild
    COMMAND cmake -E echo # as close to a no-op as cmake has
    )

add_custom_command ( OUTPUT
                         ${IMSYS_MIKA_OSWALD_FILE}
                     COMMAND ${IMSYS_MIKA_BUILD_DIR}/build-imsys.sh
                     DEPENDS always_rebuild # Always call the build script
)

add_custom_target (imsys-mika-build ALL
    DEPENDS
        ${IMSYS_MIKA_OSWALD_FILE}
)

# Make variables and targets available in parent scope if exists
get_directory_property(hasParent PARENT_DIRECTORY)
if (hasParent)
    set (IMSYS_MIKA_SOURCE_DIR ${IMSYS_MIKA_SOURCE_DIR} PARENT_SCOPE)
    set (IMSYS_MIKA_BUILD_DIR ${IMSYS_MIKA_BUILD_DIR} PARENT_SCOPE)

    add_library (imsys-mika-oswald STATIC IMPORTED GLOBAL)
    target_include_directories (imsys-mika-oswald INTERFACE ${IMSYS_MIKA_INCLUDE_DIR})
    set_target_properties (imsys-mika-oswald
        PROPERTIES
        IMPORTED_LOCATION ${IMSYS_MIKA_OSWALD_FILE}
    )
    add_dependencies (imsys-mika-oswald imsys-mika-build)

    set (imsys-mika imsys-mika-oswald PARENT_SCOPE)

endif()
