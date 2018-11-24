#include "SharedMem.h"

int main() {
	int running = 1;
	void *shared_memory = NULL;
	struct shared_use_st *shared_stuff;
	int shmid;

	shmid = sharedMem_get(1234);
	printf("shmid: %d\n", shmid);
	if (shmid == -1) {
		printf("shmget failed\n");
		exit(EXIT_FAILURE);
	}

	shared_memory = sharedMem_getAddress(shmid);
	if (shared_memory == (void *) -1) {
		printf("shmat failed\n");
		exit(EXIT_FAILURE);
	}
	printf("Memory attached at %p\n", shared_memory);
	shared_stuff = (struct shared_use_st *) shared_memory;
	shared_stuff->written_by_you = 0;
	while (running) {
		if (shared_stuff->written_by_you) {
			printf("You wrote: %s", shared_stuff->some_text);
			sleep(2); /* make the other process wait for us ! */
			shared_stuff->written_by_you = 0;
			if (strncmp(shared_stuff->some_text, "end", 3) == 0) {
				running = 0;
			}
		}
	}
	if (!sharedMem_detach(shared_memory)) {
		printf("shmdt failed\n");
		exit(EXIT_FAILURE);
	}
	if (!sharedMem_delete(shmid)) {
		printf("shmctl(IPC_RMID) failed\n");
		exit(EXIT_FAILURE);
	}
	exit(EXIT_SUCCESS);
}
