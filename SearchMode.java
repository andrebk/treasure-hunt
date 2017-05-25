enum SearchMode {
    SAFE,        // Do not chop trees or blow op tiles. Do not go between water and land
    MODERATE,    // Do not use bombs. (limited) chopping of trees and use of raft is allowed
    FREE,        // All actions are legal
    HYPOTHETICAL // Plans a route that won't be executed, to assess item needs. "Infinite" items available
}
