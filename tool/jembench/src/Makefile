.PHONY: default sources clean clobber

default:
	mkdir -p ../bld && \
	mkdir -p ${tooldeploydir}/jembench && \
	$(JAVAC) -source 1.4 -target 1.4 -d ../bld $$(find -name \*.java) && \
	$(JAVA6_HOME)bin/jar cvfe ${tooldeploydir}/jembench/jembench.jar jembench.Main -C ../bld .

sources:
	jar cvfM jembench_src.jar $$(find -name \*.java)

clean:
	rm -rf ../bld
clobber: clean
	rm -f ${tooldeploydir}/jembench/jembench.jar jembench_src.jar $$(find -name \*~)
