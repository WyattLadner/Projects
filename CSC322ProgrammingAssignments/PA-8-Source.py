# PA-8-Source.py
#
# Wyatt Ladner
#
# This program will output tables
# from a user-inputed directed
# graph using a depth first search algorithm.




def get_user_input():

    vertices_input = input("Enter vertices (e.g., A B C): ").split()
    vertices = sorted(set(vertices_input))

    print("Enter directed edges (e.g., A B for an edge from A to B). Type 'done' when finished.")
    edges = []

    while True:

        edge_input = input("Edge: ")

        if edge_input.lower() == 'done':
            break

        try:
            u, v = edge_input.split()
            
            if u in vertices and v in vertices:
                edges.append((u, v))

            else:
                print("Invalid edge, try again.")

        except:
            print("Invalid format. Use: A B")

    return vertices, edges



def build_adjacency_list(vertices, edges):

    adj_list = {v: [] for v in vertices}

    for u, v in edges:
        adj_list[u].append(v)

    return adj_list



def build_adjacency_matrix(vertices, edges):

    index_map = {v: i for i, v in enumerate(vertices)}
    size = len(vertices)
    matrix = [[0 for _ in range(size)] for _ in range(size)]

    for u, v in edges:
        i, j = index_map[u], index_map[v]
        matrix[i][j] = 1

    return matrix



def display_adjacency_list(adj_list):

    print("\nAdjacency List:")

    for k in adj_list:
        print(f"{k}: {' '.join(adj_list[k])}")



def display_adjacency_matrix(vertices, matrix):

    print("\nAdjacency Matrix:")
    print(" ", ' '.join(vertices))

    for i, row in enumerate(matrix):
        print(vertices[i], ' '.join(map(str, row)))



def dfs_main(vertices, adj_list):

    color = {v: 'white' for v in vertices}
    pred = {v: None for v in vertices}
    first_time = {v: None for v in vertices}
    last_time = {v: None for v in vertices}
    time = [0]  # Use list to make it mutable

    def dfs(u):
        color[u] = 'grey'
        first_time[u] = time[0]
        time[0] += 1
        for v in adj_list[u]:
            if color[v] == 'white':
                pred[v] = u
                dfs(v)
        color[u] = 'black'
        last_time[u] = time[0]
        time[0] += 1


    for u in vertices:
        if color[u] == 'white':
            dfs(u)

    print("\nDFS Tracking Tables:")
    print(f"{'Vertex':<8}{'Color':<8}{'Predecessor':<13}{'FirstTime':<12}{'LastTime':<10}")

    for v in vertices:
        print(f"{v:<8}{color[v]:<8}{pred[v] if pred[v] else 'None':<13}{first_time[v]:<12}{last_time[v]:<10}")



# Run Program
vertices, edges = get_user_input()
adj_list = build_adjacency_list(vertices, edges)
adj_matrix = build_adjacency_matrix(vertices, edges)
display_adjacency_list(adj_list)
display_adjacency_matrix(vertices, adj_matrix)
dfs_main(vertices, adj_list)
