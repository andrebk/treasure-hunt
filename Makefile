# Personal reminder
# $@	target
# $<	first prereq
# $?	all prereq newer than target
# $^	all prereq
# $*	stem of the rule


# File lists
AGENTSOURCE = Agent.java
GAMESOURCE = Raft.java

# Java
JC = javac
JFLAGS = -g


all: agent raft

agent:$(AGENTSOURCE)
	$(JC) $(JFLAGS) $^

raft: $(GAMESOURCE)
	$(JC) $(JFLAGS) $^

# additional targets
.PHONY: clean

clean:
	rm *.class
