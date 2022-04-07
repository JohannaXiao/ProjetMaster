#!/usr/bin/env python
# example of proof of work algorithm

import hashlib
import time
import pandas as pd

max_nonce = 2 ** 32  # 4 billion

block_map = {}


def get_block_from_hash(hash):
    return block_map[hash]


def proof_of_work(block, difficulty_bits):
    # calculate the difficulty target
    target = 2 ** (256 - difficulty_bits)

    for nonce in range(max_nonce):
        hash_result = hashlib.sha256((str(block.get_raw()) + str(nonce)).encode('utf-8')).hexdigest()
        # check if this is a valid result ,below the target

        if int(hash_result, 16) < target:
            block.update_nonce(nonce)
            block.update_hash(hash_result)
            block_map[block.hash] = block
            print("Success with nonce %d" % nonce)
            print
            "block is %s" % block.string()

            return block

    print("Failed after %d (max_nonce) tries" % nonce)
    return nonce


class Block:

    def __init__(self, hash, prehash, difficulty):
        self.hash = hash
        self.prehash = prehash
        self.txs = []
        self.difficulty = difficulty
        self.nonce = 0

        if self.prehash != '0x0':
            block = get_block_from_hash(self.prehash)
            self.worldstate = block.worldstate
        else:
            self.worldstate = {}

    def add_tx(self, tx):
        self.txs.append(tx)

    # def update_workdstate():
    # 	for tx in self.txs:
    # 		self.worldstate[tx.from] -= tx.value
    # 		self.worldstate[tx.to] += tx.value

    def update_nonce(self, nonce):
        self.nonce = nonce

    def update_hash(self, hash):
        self.hash = hash

    def get_raw(self):
        return self.hash + self.prehash + str(self.difficulty)

    def string(self):
        print("[block hash:%s, prehash:%s, difficulty:%s, nonce:%s]" % (self.hash, self.prehash, self.difficulty, self.nonce))


def send_block(block):
    pass


if __name__ == '__main__':
    nonce = 0
    mined_block = Block('0x0', '0x0', 0)
    Elapsed_times=[]
    Hashing_Powers=[]

    # difficulty from 0 to 31 bits
    for difficulty_bits in range(32):
        difficulty = 2 ** difficulty_bits

        print("")
        print("==================satrt mining=====================")
        print("Difficulty: %d bits" % difficulty_bits)

        print("Start Searching...")

        # check the current time
        start_time = time.time()

        # make a new block which includes the hash from the previous block
        # we fake a block of trasactions - just a string

        new_block = Block('', mined_block.hash, difficulty)

        # find a valid nonce for the new block
        mined_block = proof_of_work(new_block, difficulty_bits)

        # checkpoint how long it took find a result
        end_time = time.time()

        elapsed_time = end_time - start_time
        Elapsed_times.append(elapsed_time)
        print("Elapsed Time: %.4f seconds" % elapsed_time)

        if elapsed_time > 0:
            # estimate the hash per second
            hash_power = float(int(mined_block.nonce) / elapsed_time)
            Hashing_Powers.append(hash_power)
            print("Hashing Power: %ld hashes per second" % hash_power)
        name = ['elapsed_time','hash_power']
        test = pd.DataFrame(list(zip(Elapsed_times,Hashing_Powers)),columns=name)
        test.to_csv('Proof_of_work_data.csv')