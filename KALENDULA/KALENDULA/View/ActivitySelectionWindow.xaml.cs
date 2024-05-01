using System;
using System.Collections.Generic;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using Newtonsoft.Json;
using System.Windows.Media.Imaging;
using KALENDULA;
using System.Linq;
using KALENDULA.ViewModel;


namespace KALENDAR
{

    public partial class ActivitySelectionWindow : Window
    {
        private DateTime selectedDate;
        private Dictionary<DateTime, List<string>> activities;
        private const string FileName = "activities.json";
       


        public ActivitySelectionWindow(DateTime selectedDate, MainWindow mainWindow)
        {
            InitializeComponent();
            this.selectedDate = selectedDate;
            activities = LoadActivitiesFromFile();
            
            DataContext = new ActivitySelectionViewModel(selectedDate, mainWindow);
            List<string> selectedActivities = GetSelectedActivitiesForDate(selectedDate);
            LoadActivities(selectedActivities);

        }

        public ActivitySelectionWindow(DateTime selectedDate, KALENDULA.ViewModel.MainWindowViewModel mainWindowViewModel)
        {
            this.selectedDate = selectedDate;
        }

        private void LoadActivities(List<string> selectedActivities)
        {
            stackPanelActivities.Children.Clear();

            List<string> activityList = new List<string> { "CS2", "Dota2", "Palworld", "Minecraft", "Весёлая  ферма", "DayZ" };
            List<string> imagePaths = new List<string> { "cs2.png", "dota2.png", "palworld.png", "minecraft.png", "farm.png", "dayz.png" };

            foreach (var activity in activityList)
            {
                int index = activityList.IndexOf(activity);
                string imageFileName = imagePaths[index];

                // Получаем поток ресурса изображения
                Uri imageUri = new Uri("pack://application:,,,/Images/" + imageFileName, UriKind.Absolute);

                StackPanel gamePanel = new StackPanel { Orientation = Orientation.Horizontal };

                Image image = new Image
                {
                    Source = new BitmapImage(imageUri),
                    Width = 50,
                    Height = 50
                };

                CheckBox checkBox = new CheckBox
                {
                    Content = activity,
                    IsChecked = selectedActivities.Contains(activity)
                };

                gamePanel.Children.Add(image);
                gamePanel.Children.Add(checkBox);

                stackPanelActivities.Children.Add(gamePanel);
            }
        }

        private void BtnSave_Click(object sender, RoutedEventArgs e)
        {
            List<string> selectedActivities = new List<string>();
            foreach (StackPanel gamePanel in stackPanelActivities.Children)
            {
                CheckBox checkBox = (CheckBox)gamePanel.Children[1];
                if (checkBox.IsChecked == true)
                {
                    selectedActivities.Add(checkBox.Content.ToString());
                }
            }
            activities[selectedDate] = selectedActivities;
            SaveActivitiesToFile();
            MainWindow mainWindow = Application.Current.MainWindow as MainWindow;
            if (mainWindow != null)
            {
                mainWindow.UpdateDayButton(selectedDate, selectedActivities.FirstOrDefault() ?? "Нет игр");
            }

            Close();
        }

        private void BtnCancel_Click(object sender, RoutedEventArgs e)
        {
            Close();
        }

        private Dictionary<DateTime, List<string>> LoadActivitiesFromFile()
        {
            if (File.Exists(FileName))
            {
                string json = File.ReadAllText(FileName);
                return JsonConvert.DeserializeObject<Dictionary<DateTime, List<string>>>(json) ?? new Dictionary<DateTime, List<string>>();
            }
            return new Dictionary<DateTime, List<string>>();
        }

        private void SaveActivitiesToFile()
        {
            string json = JsonConvert.SerializeObject(activities, Formatting.Indented);
            File.WriteAllText(FileName, json);
        }

        private List<string> GetSelectedActivitiesForDate(DateTime date)
        {
            if (activities.TryGetValue(date, out List<string> selectedActivities))
            {
                return selectedActivities;
            }
            return new List<string>();
        }
    }
}