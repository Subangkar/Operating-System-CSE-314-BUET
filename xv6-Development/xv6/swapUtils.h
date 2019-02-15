//
// Created by Subangkar on 10-Feb-19.
//

#ifndef XV6_SWAP_SWAPUTILS_H
#define XV6_SWAP_SWAPUTILS_H

/*
* Returns the physical address mapped to the virtual address vAddr in the page-dir
*/
uint getPhysicalAddress(int vAddr, pde_t *pgdir);

/*
* Gets the index of page in memory which should be swapped-out according to the defined policy
*/
int nextSwapPageIndexInRAM(struct proc *p);
/*
* Finds an available room for page in memory and returns its index
*/
int nextFreePageIndexInRAM(struct proc *p);


/*
* Swaps a single page between memory and file by finding the page to swap-out,
* finding a available place in file for the above and put it in it.
*/
void swap_out(struct proc *p, pde_t *pgdir, uint vAddr);
/*
* Reads a page corresponding to page_index from swapfile,
* Allocates new page in physical memory for it and writes it into memory
*/
int swap_in(struct proc* p, int page_index);

// =============================== Page Table I/O ===============================
/*
* Checks if page corresponding to vAddr is indeed in swapfile (e.g not in memory)
*/
bool isPageInSwapFile(struct proc *p, int vAddr);
/*
* Change PTE flags properly after swapping-out vAddr
*/
void update_pageOUT_pte_flags(struct proc* p, int vAddr, pde_t * pgdir);
/*
* Updates PTE flags of vAddr after swapping-in a page
*/
void update_pageIN_pte_flags(struct proc* p, int vAddr, int pagePAddr, pde_t * pgdir);
// =============================== -------------- ===============================


// =============================== RAM I/O ===============================
/*
* Finds an available page in memory and updates its virtual address to vAddr, etc.
*/
void insertPageToRAM(struct proc *p, pde_t *pgdir, addr_t vAddr);

/// actually do not free the physical
void removePageFromRAM(struct proc *p, addr_t vAddr, const pde_t *pgdir);

/// actually do not actually free the physical only from manager
void deletePageFromRAM(struct proc *p, int index, bool reform);
// =============================== ------- ===============================

// =============================== FIFO ===============================
bool isFIFOfull(const struct proc* p);
bool isFIFOEmpty(const struct proc* p);
int nextFIFOFreePageIndex(const struct proc *p);
int nextFIFOSwapPageIndex(const struct proc *p);
void updateFIFONextFreePageIndex(struct proc *p);
void organizeFIFORAMqueue(struct proc *p,int from);
/// assumes queue is full
void shiftFIFOIndex(struct proc* p);
// =============================== --- ================================

// =============================== DISK I/O ===============================
int page_out(struct proc * p, addr_t vAddr, pde_t *pgdir);
int page_in(struct proc * p, int ram_managerIndex, addr_t vAddr, char* buff);
// =============================== -------- ===============================

#endif //XV6_SWAP_SWAPUTILS_H
