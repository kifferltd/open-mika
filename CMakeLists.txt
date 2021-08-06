# Simple CMake wrapper for building Mika for the imsys target using the build-imsys.sh.in script
cmake_minimum_required(VERSION 3.16)

project(imsys-mika NONE)

if (NOT CMAKE_BUILD_TYPE)
  set (CMAKE_BUILD_TYPE Release)
endif ()

string(TOUPPER "${CMAKE_BUILD_TYPE}" CMAKE_BUILD_TYPE)

if (NOT IMSYS_MIKA_REBUILD)
    set (IMSYS_MIKA_REBUILD Always)
endif ()

string(TOUPPER "${IMSYS_MIKA_REBUILD}" IMSYS_MIKA_REBUILD)

set (IMSYS_ISAL_PHASE 1 CACHE STRING "ISAL implementation phase")

set (IMSYS_MIKA_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR})
set (IMSYS_MIKA_BUILD_DIR ${CMAKE_CURRENT_BINARY_DIR})

if( NOT ${IMSYS_MIKA_BUILD_DIR} STREQUAL ${IMSYS_MIKA_SOURCE_DIR}/build)
  message( FATAL_ERROR "Mika build directory must be sw-open-mika/build" )
endif()

configure_file (${IMSYS_MIKA_SOURCE_DIR}/build-imsys.sh.in ${IMSYS_MIKA_BUILD_DIR}/build-imsys.sh @ONLY)

set (IMSYS_MIKA_INCLUDE_DIR ${IMSYS_MIKA_SOURCE_DIR}/core-vm/include)
set (IMSYS_MIKA_LIB_DIR ${IMSYS_MIKA_BUILD_DIR}/im4000/lib)
set (IMSYS_MIKA_LIBOSWALD_FILE ${IMSYS_MIKA_LIB_DIR}/liboswald.a)
set (IMSYS_MIKA_LIBMIKA_FILE ${IMSYS_MIKA_LIB_DIR}/libmika.a)
set (IMSYS_MIKA_LIBAWT_FILE ${IMSYS_MIKA_LIB_DIR}/libawt.a)

if (${IMSYS_MIKA_REBUILD} STREQUAL ALWAYS)
    # Dummy output which is never actually produced. Anything that depends on
    # this will always be rebuilt.
    add_custom_command(
        OUTPUT always_rebuild
        COMMAND cmake -E echo # as close to a no-op as cmake has
        )
    # Make the build script depend on this to always call it.
    set (IMSYS_MIKA_BUILD_DEP always_rebuild)
elseif (${IMSYS_MIKA_REBUILD} STREQUAL NEVER)
    # Do not define dependency for the build script,
    # it will be called only if at least one output library file is missing.
    set (IMSYS_MIKA_BUILD_DEP "")
else ()
    message(SEND_ERROR "Unexpected rebuild mode (IMSYS_MIKA_REBUILD)")
endif ()

add_custom_command ( OUTPUT
                         ${IMSYS_MIKA_LIBOSWALD_FILE}
                         ${IMSYS_MIKA_LIBMIKA_FILE}
                         ${IMSYS_MIKA_LIBAWT_FILE}
                     COMMAND ${IMSYS_MIKA_BUILD_DIR}/build-imsys.sh
                     DEPENDS ${IMSYS_MIKA_BUILD_DEP}
)

add_custom_target (imsys-mika-build ALL
    DEPENDS
        ${IMSYS_MIKA_LIBOSWALD_FILE}
        ${IMSYS_MIKA_LIBMIKA_FILE}
        ${IMSYS_MIKA_LIBAWT_FILE}
)

# Make variables and targets available in parent scope if exists
get_directory_property(hasParent PARENT_DIRECTORY)
if (hasParent)
    set (IMSYS_MIKA_SOURCE_DIR ${IMSYS_MIKA_SOURCE_DIR} PARENT_SCOPE)
    set (IMSYS_MIKA_BUILD_DIR ${IMSYS_MIKA_BUILD_DIR} PARENT_SCOPE)

    set (IMSYS_MIKA_INCLUDE_DIR ${IMSYS_MIKA_INCLUDE_DIR} PARENT_SCOPE)

    add_library (imsys-mika-liboswald STATIC IMPORTED GLOBAL)
    target_include_directories (imsys-mika-liboswald INTERFACE ${IMSYS_MIKA_INCLUDE_DIR})
    set_target_properties (imsys-mika-liboswald
        PROPERTIES
        IMPORTED_LOCATION ${IMSYS_MIKA_LIBOSWALD_FILE}
    )
    add_dependencies (imsys-mika-liboswald imsys-mika-build)

    add_library (imsys-mika-libmika STATIC IMPORTED GLOBAL)
    target_include_directories (imsys-mika-libmika INTERFACE ${IMSYS_MIKA_INCLUDE_DIR})
    set_target_properties (imsys-mika-libmika
        PROPERTIES
        IMPORTED_LOCATION ${IMSYS_MIKA_LIBMIKA_FILE}
    )
    add_dependencies (imsys-mika-libmika imsys-mika-build)

    add_library (imsys-mika-libawt STATIC IMPORTED GLOBAL)
    target_include_directories (imsys-mika-libawt INTERFACE ${IMSYS_MIKA_INCLUDE_DIR})
    set_target_properties (imsys-mika-libawt
        PROPERTIES
        IMPORTED_LOCATION ${IMSYS_MIKA_LIBAWT_FILE}
    )
    add_dependencies (imsys-mika-libawt imsys-mika-build)

    set (imsys-mika imsys-mika-liboswald imsys-mika-libmika imsys-mika-libawt PARENT_SCOPE)

endif()
