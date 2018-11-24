#include "SharedMem.h"

int main() {
	int running = 1;
	char buffer[BUFSIZ];
	int shmid = sharedMem_get(1234);
	if (shmid == -1) {
		printf("shmget failed\n");
		exit(EXIT_FAILURE);
	}

	shared_use_st *shared_memory = (struct shared_use_st *) sharedMem_getAddress(shmid);
	if (shared_memory == (void *) -1)exit(EXIT_FAILURE);
	printf("Memory attached at %p\n", shared_memory);

	while (running) {
		while (shared_memory->written_by_you == 1) {
			sleep(1);
			printf("waiting for client...\n");
		}
		printf("Enter some text: ");
		fgets(buffer, BUFSIZ, stdin);
		sharedMem_write(shared_memory, buffer);
		if (strncmp(buffer, "$end", 4) == 0 || strncmp(buffer, "end", 3) == 0) {
			running = 0;
		}
	}

	if (!sharedMem_detach(shared_memory) ){
		printf("shmdt failed\n");
		exit(EXIT_FAILURE);
	}
	exit(EXIT_SUCCESS);
}
