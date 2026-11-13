package com.agri.reclamation.entity;

public enum ReclamationStatus {
    OPEN,         // just created by the consumer
    IN_PROGRESS,  // the admin is looking at it
    RESOLVED,     // the admin solved it  -> event sent to Ordering
    REJECTED      // the admin refused it
}
