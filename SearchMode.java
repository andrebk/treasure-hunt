/* Different search modes to modify the behaviour of the agent, by dictating which actions it is allowed to take.
 * Used in the SearchState class */
enum SearchMode {
    SAFE,        // Do not chop trees or blow op tiles. Do not go between water and land
    MODERATE,    // Do not use bombs. (limited) chopping of trees and use of raft is allowed
    FREE        // All actions are legal
}
