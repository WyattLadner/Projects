# PA-9-Source
#
# Wyatt Ladner
#
# This program will use
# the SCC algorithm
# to show strongly connected
# components from user input


from collections import defaultdict

class Graph:
    def __init__(self):
        self.graph = defaultdict(list)
        self.nodes = set()

    def add_edge(self, u, v):
        self.graph[u].append(v)
        self.nodes.update([u, v])

    def _dfs(self, v, visited, stack=None, component=None):
        visited.add(v)
        if component is not None:
            component.append(v)
        for neighbor in self.graph[v]:
            if neighbor not in visited:
                self._dfs(neighbor, visited, stack, component)
        if stack is not None:
            stack.append(v)

    def get_transpose(self):
        transpose = Graph()
        for u in self.graph:
            for v in self.graph[u]:
                transpose.add_edge(v, u)
        return transpose

    def find_sccs(self):
        visited = set()
        stack = []

        # Step 1: Fill stack according to finish times
        for node in sorted(self.nodes):  # sorted to ensure consistent output
            if node not in visited:
                self._dfs(node, visited, stack)

        # Step 2: Reverse the graph
        transpose = self.get_transpose()

        # Step 3: DFS on transposed graph in stack order
        visited.clear()
        sccs = []
        while stack:
            node = stack.pop()
            if node not in visited:
                component = []
                transpose._dfs(node, visited, component=component)
                sccs.append(component)
        return sccs

def main():
    print("Strongly Connected Components (SCC) Finder using Kosaraju's Algorithm\n")
    
    g = Graph()

    # Get nodes
    nodes = input("Enter the node labels (e.g., A B C D): ").split()
    
    print("\nNow enter directed edges (e.g., A B means A → B). Type 'done' when finished.")
    while True:
        edge = input("Enter edge (from to): ")
        if edge.lower() == "done":
            break
        try:
            u, v = edge.split()
            g.add_edge(u, v)
        except:
            print("Invalid input. Please enter two node labels separated by space.")

    print("\nFinding strongly connected components...")
    sccs = g.find_sccs()

    print("\nStrongly Connected Components:")
    for i, component in enumerate(sccs, 1):
        print(f"Component {i}: {' '.join(component)}")

if __name__ == "__main__":
    main()
