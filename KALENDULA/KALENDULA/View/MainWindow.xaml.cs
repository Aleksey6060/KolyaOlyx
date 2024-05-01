using System;
using System.Collections.Generic;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Media.Imaging;
using KALENDAR;
using Newtonsoft.Json;

namespace KALENDULA
{
    public partial class MainWindow : Window
    {
        private DateTime currentDate;

        public MainWindow()
        {
            InitializeComponent();
            currentDate = DateTime.Today;
            UpdateCalendar();
           
        }

        private void CreateDayButton(int day, int column, int row)
        {
            Button dayButton = new Button() { Content = day.ToString(), Margin = new Thickness(5), Style = (Style)FindResource("DayButtonStyle") };
            Grid.SetColumn(dayButton, column);
            Grid.SetRow(dayButton, row);
            dayButton.Click += DayButton_Click;
            dayButton.ContextMenu = (ContextMenu)FindResource("DayContextMenu");
            dayButton.Tag = new DateTime(currentDate.Year, currentDate.Month, day);
            calendarGrid.Children.Add(dayButton);
        }

        private void UpdateCalendar()
        {
            calendarGrid.Children.Clear();
            calendarGrid.RowDefinitions.Clear();
            calendarGrid.ColumnDefinitions.Clear();

            int daysInMonth = DateTime.DaysInMonth(currentDate.Year, currentDate.Month);
            DayOfWeek firstDayOfWeek = currentDate.DayOfWeek;

            // Add columns for each day of the week
            for (int i = 0; i < 7; i++)
            {
                calendarGrid.ColumnDefinitions.Add(new ColumnDefinition() { Width = new GridLength(1, GridUnitType.Star) });
            }

            // Add day names
            string[] dayNames = { "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс" };
            for (int i = 0; i < dayNames.Length; i++)
            {
                Button dayButton = new Button() { Content = dayNames[i], IsEnabled = false, Background = Brushes.LightGray };
                Grid.SetColumn(dayButton, i);
                calendarGrid.Children.Add(dayButton);
            }

            // Calculate the number of rows needed
            int totalDays = daysInMonth + (int)firstDayOfWeek;
            int totalWeeks = (totalDays + 6) / 7; // Add 6 to round up

            // Add rows
            for (int i = 0; i < totalWeeks; i++)
            {
                calendarGrid.RowDefinitions.Add(new RowDefinition() { Height = new GridLength(1, GridUnitType.Star) });
            }

            // Add day buttons
            int day = 1;
            for (int row = 1; row <= totalWeeks; row++)
            {
                for (int column = 0; column < 7; column++)
                {
                    if (row == 1 && column < (int)firstDayOfWeek)
                    {
                        // Fill in the empty spaces before the first day of the month
                        calendarGrid.Children.Add(new Button() { IsEnabled = false, Background = Brushes.LightGray });
                        Grid.SetColumn(calendarGrid.Children[calendarGrid.Children.Count - 1], column);
                        Grid.SetRow(calendarGrid.Children[calendarGrid.Children.Count - 1], row);
                    }
                    else if (day <= daysInMonth)
                    {
                        // Create a day button
                        CreateDayButton(day, column, row);
                        day++;
                    }
                    else
                    {
                        // Fill in the empty spaces after the last day of the month
                        calendarGrid.Children.Add(new Button() { IsEnabled = false, Background = Brushes.LightGray });
                        Grid.SetColumn(calendarGrid.Children[calendarGrid.Children.Count - 1], column);
                        Grid.SetRow(calendarGrid.Children[calendarGrid.Children.Count - 1], row);
                    }
                }
            }

            txtMonthYear.Text = currentDate.ToString("MMMM yyyy");
        }

        private void DayButton_Click(object sender, RoutedEventArgs e)
        {
            Button clickedButton = (Button)sender;
            DateTime selectedDate = (DateTime)clickedButton.Tag;

            System.Diagnostics.Debug.WriteLine($"DayButton_Click called for date: {selectedDate}");

            if (e.OriginalSource is ContextMenu)
            {
                ContextMenu contextMenu = (ContextMenu)clickedButton.ContextMenu;
                contextMenu.PlacementTarget = clickedButton;
                contextMenu.IsOpen = true;
            }
            else
            {
                System.Diagnostics.Debug.WriteLine("OpenDay_Click called");
                OpenDay_Click(sender, e);
            }
        }

        private void OpenDay_Click(object sender, RoutedEventArgs e)
        {
            Button dayButton = (Button)sender;
            DateTime selectedDate = (DateTime)dayButton.Tag;

            try
            {
                // Передаем текущий экземпляр MainWindow в ActivitySelectionWindow
                ActivitySelectionWindow activityWindow = new ActivitySelectionWindow(selectedDate, this);
                activityWindow.ShowDialog();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Произошла ошибка при открытии окна ActivitySelectionWindow: " + ex.Message);
            }
        }



        private void ClearDay_Click(object sender, RoutedEventArgs e)
        {
            MenuItem menuItem = (MenuItem)sender;
            ContextMenu contextMenu = (ContextMenu)menuItem.Parent;
            Button dayButton = (Button)contextMenu.PlacementTarget;
            DateTime selectedDate = (DateTime)dayButton.Tag;

            string jsonFilePath = "activities.json";

            if (File.Exists(jsonFilePath))
            {
                string jsonString = File.ReadAllText(jsonFilePath);

                List<Activity> activities = JsonConvert.DeserializeObject<List<Activity>>(jsonString);

                activities.RemoveAll(activity => activity.Date.Date == selectedDate.Date);

                string updatedJsonString = JsonConvert.SerializeObject(activities, Newtonsoft.Json.Formatting.Indented);

                File.WriteAllText(jsonFilePath, updatedJsonString);
            }
            else
            {
                MessageBox.Show("Файл activities.json не найден.");
            }
        }

        public void SetButtonImage(DateTime date, BitmapImage image)
        {
            foreach (UIElement element in calendarGrid.Children)
            {
                if (element is Button button && button.Tag is DateTime buttonDate && buttonDate.Date == date.Date)
                {
                    button.Content = new Image
                    {
                        Source = image,
                        Width = 50,
                        Height = 50
                    };
                    break;
                }
            }
        }

        private void NavigateBack_Click(object sender, RoutedEventArgs e)
        {
            currentDate = currentDate.AddMonths(-1);
            RotateTransform rotateTransform = new RotateTransform();

            DoubleAnimation animation = new DoubleAnimation(0, 360, new Duration(TimeSpan.FromSeconds(0.5)));
            rotateTransform.BeginAnimation(RotateTransform.AngleProperty, animation);

            calendarGrid.RenderTransform = rotateTransform;
            UpdateCalendar();
        }

        private void NavigateForward_Click(object sender, RoutedEventArgs e)
        {
            currentDate = currentDate.AddMonths(1);

            RotateTransform rotateTransform = new RotateTransform();

            DoubleAnimation animation = new DoubleAnimation(0, 360, new Duration(TimeSpan.FromSeconds(0.5)));
            rotateTransform.BeginAnimation(RotateTransform.AngleProperty, animation);

            calendarGrid.RenderTransform = rotateTransform;

            UpdateCalendar();
        }

        public void UpdateDayButton(DateTime date, string activityName)
        {
            foreach (UIElement element in calendarGrid.Children)
            {
                if (element is Button button && button.Tag is DateTime buttonDate && buttonDate.Date == date.Date)
                {
                    button.Content = activityName;
                    break;
                }
            }
        }


    }

    public class Activity
    {
        public DateTime Date { get; set; }
    }
}