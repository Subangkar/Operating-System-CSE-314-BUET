//
// Created by subangkar on 11/24/18.
//

#ifndef SEMAPHORE_SEM_H
#define SEMAPHORE_SEM_H

#include <unistd.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <cstdio>

#define CHANGE_PER_PROCESS_ENTRY (1)

typedef int semaphoreID_t;

// creates new
int semaphore_create(key_t key, int no_of_Sems = 1) {
	return semget(key, no_of_Sems, 0666 | IPC_CREAT | IPC_EXCL);
}

// Create or get the existing one
int semaphore_get(key_t key, int no_of_Sems = 1) {
	return semget(key, no_of_Sems, 0666 | IPC_CREAT);
}

// initialize
bool semaphore_setValue(semaphoreID_t sem_id, int semNo = 0, int no_of_allowedSems = 1) {
	return semctl(sem_id, semNo, SETVAL, no_of_allowedSems) != EOF;
}

// removes
bool semaphore_delete(semaphoreID_t sem_id, int semNo = 0) {
	return semctl(sem_id, semNo, IPC_RMID) != EOF;
}

// P()
bool semaphore_P(semaphoreID_t sem_id, unsigned short semNo = 0, size_t nOps = 1) {
	sembuf changeVal = {semNo, -CHANGE_PER_PROCESS_ENTRY, SEM_UNDO};
	return semop(sem_id, &changeVal, nOps) != EOF;
}

// V()
static bool semaphore_V(semaphoreID_t sem_id, unsigned short semNo = 0, size_t nOps = 1) {
	sembuf changeVal = {semNo, CHANGE_PER_PROCESS_ENTRY, SEM_UNDO};
	return semop(sem_id, &changeVal, nOps) != EOF;
}

#endif //SEMAPHORE_SEM_H
