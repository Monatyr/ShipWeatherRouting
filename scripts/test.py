import seaborn as sns
import pandas as pd
import matplotlib.pyplot as plt

# Example data: Pareto solutions with three objectives
data = {
    "Objective 1": [1.2, 2.1, 1.8, 1.5, 2.0],
    "Objective 2": [2.3, 1.5, 1.8, 2.0, 1.3],
    "Objective 3": [3.0, 2.5, 2.8, 2.7, 2.2]
}

df = pd.DataFrame(data)

# Use seaborn to create a pairplot
sns.pairplot(df)
plt.suptitle("Pairwise Comparison of Pareto Solutions", y=1.02)
plt.show()
