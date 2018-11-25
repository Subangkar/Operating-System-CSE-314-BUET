//
// Created by subangkar on 11/24/18.
//
#include <unistd.h>
#include <cstdlib>
#include <cstdio>
#include <cstring>
#include <sys/shm.h>
#include <string>

#ifndef SHAREDMEM_SHAREDMEM_H
#define SHAREDMEM_SHAREDMEM_H

#define NO_OF_ELEMS 100
#define TEXT_SZ 2048
struct shared_use_st {
//	int written_by_you;
//	char some_text[TEXT_SZ];
	int writtenBy = 0;
	std::string cmd;
	int buffer[NO_OF_ELEMS];
	int buf_size=0;
};

// return -1 on failure
int sharedMem_create(key_t key, size_t dataSize = sizeof(shared_use_st)) {
	return shmget(key, dataSize, 0666 | IPC_CREAT | IPC_EXCL);
}

// return -1 on failure
int sharedMem_get(key_t key, size_t dataSize = sizeof(shared_use_st)) {
	return shmget(key, dataSize, 0666 | IPC_CREAT);
}

// return -1 on failure
void *sharedMem_getAddress(int sharedMemID, void *attach_to_address = NULL, int attachMode = 0) {
	return shmat(sharedMemID, attach_to_address, attachMode);
}

// detach but not delete
bool sharedMem_detach(void *address) {
	return shmdt(address) != EOF;
}

//
bool sharedMem_readStat(int sharedMemID, shmid_ds *perms) {
	return shmctl(sharedMemID, IPC_STAT, perms) != EOF;
}

//
//bool sharedMem_setPermissions(int sharedMemID) {
//	return shmctl(sharedMemID, IPC_SET, 0) != EOF;
//}

// deletes
bool sharedMem_delete(int sharedMemID) {
	return shmctl(sharedMemID, IPC_RMID, 0) != EOF;
}

//void sharedMem_write(shared_use_st *address, const char *text) {
//	strncpy(address->some_text, text, TEXT_SZ);
//	address->written_by_you = 1;
//}

#endif //SHAREDMEM_SHAREDMEM_H
