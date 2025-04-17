# PA 7
# CSC 322
# 
# Wyatt Ladner
#
# This program will create a graph
# using BFS for nodes entered by
# the user.

from collections import deque
import string

class Graph:

    # Creates the graph
    def __init__(self, num_nodes):
        self.num_nodes = num_nodes
        self.letters = list(string.ascii_uppercase[:num_nodes])  # Generate node labels (A, B, C, ...)
        self.letter_to_index = {letter: i for i, letter in enumerate(self.letters)}
        self.index_to_letter = {i: letter for i, letter in enumerate(self.letters)}

        self.adj_matrix = [[0] * num_nodes for _ in range(num_nodes)]
        self.adj_list = {letter: [] for letter in self.letters}


    # Adds edge to graph
    def add_edge(self, u, v):
        u_idx, v_idx = self.letter_to_index[u], self.letter_to_index[v]
        self.adj_matrix[u_idx][v_idx] = 1
        self.adj_matrix[v_idx][u_idx] = 1  # Undirected graph
        self.adj_list[u].append(v)
        self.adj_list[v].append(u)


    # Forms and displays matrix
    def display_adjacency_matrix(self):
        print("\nAdjacency Matrix:")
        print("  " + " ".join(self.letters))  # Print column headers
        for i, row in enumerate(self.adj_matrix):
            print(self.letters[i], " ".join(map(str, row)))  # Row header + data


    # Forms and displays list
    def display_adjacency_list(self):
        print("\nAdjacency List:")
        for node, neighbors in self.adj_list.items():
            print(f"{node}: {neighbors}")


    # Performs BFS
    def bfs(self, start_letter):

        print(f"\nBFS Traversal from Node {start_letter}:")

        visited = {letter: False for letter in self.letters}
        distance = {letter: -1 for letter in self.letters}
        predecessor = {letter: None for letter in self.letters}
        queue = deque([start_letter])

        visited[start_letter] = True
        distance[start_letter] = 0

        print("\nStep-by-step BFS:")
        while queue:
            print(f"Queue: {list(queue)}")
            current = queue.popleft()
            print(f"Visiting: {current}")

            for neighbor in self.adj_list[current]:
                if not visited[neighbor]:
                    visited[neighbor] = True
                    distance[neighbor] = distance[current] + 1
                    predecessor[neighbor] = current
                    queue.append(neighbor)

        # Display BFS tables
        print("\nTracking Tables:")
        print(f"Node:        {list(self.letters)}")
        print(f"Distance:    {[distance[node] for node in self.letters]}")
        print(f"Predecessor: {[predecessor[node] if predecessor[node] else '-' for node in self.letters]}")



# Main function to interact with the user
def main():
    num_nodes = int(input("Enter number of nodes: "))
    graph = Graph(num_nodes)

    num_edges = int(input("Enter number of edges: "))
    print(f"Enter edges as pairs of space-separated letters (e.g., A B):")

    for _ in range(num_edges):
        u, v = input().split()
        graph.add_edge(u, v)

    graph.display_adjacency_matrix()
    graph.display_adjacency_list()

    start_node = input("\nEnter the source node for BFS: ").strip()
    graph.bfs(start_node)

if __name__ == "__main__":
    main()