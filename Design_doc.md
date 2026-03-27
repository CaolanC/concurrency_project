# Warehouse Simulation Design

**Video link:**  #TO NOT FORGET 

## 1) Overview
This project simulates a concurrent warehouse where deliveries arrive over time, stockers move boxes from a staging area into sections, and pickers remove boxes from sections. Time is modeled in ticks (1000 ticks/day), and each actor thread uses a shared simulation clock to keep timing consistent.

Current implementation status: core simulation loop and logging are in place.

## 2) List of working functionality

| Description | Status | Notes |
|---|---|---|
| Shared tick-based simulation clock | Working | Single `SimulationClock` thread; all actors use `sleepTicks(...)`. |
| Tick duration configurable via JSON | Working | Reads `ms_per_tick` (fallback `tick_time_ms`) with minimum 50 ms. |
| Delivery generation | Working | Delivery thread uses per-tick probability and display `delivery_arrived`. |
| Staging area concurrency | Working | Synchronized updates and synchronized take operations. |
| Multiple stocker threads | Working | Configurable `stockers` count; each stocker carries up to 10 boxes. |
| Picker threads + random fixed section per attempt | Working | Pickers select one section per attempt, log `pick_start` before waiting, and log `pick_done` with `waited_ticks`. |
| Section capacity from config | Working | `Section` now uses per-section `capacity` from JSON. |
| Section lock model (pickers vs stockers) | Partly working | Locking primitives exist, but full end-to-end stocker -> section behavior is not finished. |
| Stocker priority route choice | Partly working | Stockers prioritize by carried load; picker-wait-aware prioritisation not yet implemented. |
| Trolleys | Not working | Not implemented yet. |
| Stocker breaks | Not working | Not implemented yet. |

## 3) Division of work
