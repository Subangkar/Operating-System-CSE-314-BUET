#include<unistd.h>
#include<stdlib.h>
#include<stdio.h>
#include<string.h>
#include <sys/shm.h>

#include "shm_com.h"
int main()
{
	int running = 1;
	void *shared_memory = NULL;
	struct shared_use_st *shared_stuff;
	int shmid;
	shmid = shmget((key_t)1234, sizeof(struct shared_use_st), 0666 | IPC_CREAT);
	if (shmid == -1) {
		printf("shmget failed\n");
		exit(EXIT_FAILURE);
	}
	shared_memory = shmat(shmid, NULL, 0);
	if (shared_memory == (void *)-1) {
		printf("shmat failed\n");
		exit(EXIT_FAILURE);
	}
	printf("Memory attached at %p\n", shared_memory);	
	shared_stuff = (struct shared_use_st *)shared_memory;
	shared_stuff->written_by_you = 0;
	while(running) {
		if (shared_stuff->written_by_you) {
			printf("You wrote: %s", shared_stuff->some_text);
			sleep(4); /* make the other process wait for us ! */
			shared_stuff->written_by_you = 0;
			if (strncmp(shared_stuff->some_text, "end", 3) == 0) {
				running = 0;
			}
		}
	}
	if (shmdt(shared_memory) == -1) {
		printf("shmdt failed\n");
		exit(EXIT_FAILURE);
	}
	if (shmctl(shmid, IPC_RMID, 0) == -1) {
		printf("shmctl(IPC_RMID) failed\n");
		exit(EXIT_FAILURE);
	}
	exit(EXIT_SUCCESS);
}
