using System;
using System.Windows;
using System.Windows.Controls;
using ImapX;
using ImapX.Collections;

namespace Exel
{
    public partial class pochta : Window
    {
        public pochta()
        {
            InitializeComponent();
            EmailTxt.Text = "Введите почту";
            EmailTxt.GotFocus += RemoveText;
            EmailTxt.LostFocus += AddText;
            PasswordTxt.PasswordChanged += PasswordChanged;
        }

        internal class ImapHelper
        {
            private static ImapClient client { get; set; }
            public static void Initialize(string host)
            {
                client = new ImapClient(host, true);
                if (!client.Connect())
                {
                    throw new Exception("Не удалось подключиться!");
                }
            }

            public static bool Login(string u, string p)
            {
                return client.Login(u, p);
            }

            public static void Logout()
            {
                if (client.IsAuthenticated)
                {
                    client.Logout();
                    client.Dispose();
                }
            }

            public static CommonFolderCollection GetFolders()
            {
                client.Folders.Inbox.StartIdling();
                client.Folders.Inbox.OnNewMessagesArrived += Inbox_OnNewMessagesArrived;
                return client.Folders;
            }

            private static void Inbox_OnNewMessagesArrived(object sender, IdleEventArgs e)
            {
                MessageBox.Show($"Пришло новое сообщение в папку {e.Folder.Name}");
            }

            public static MessageCollection GetMessagesForFolder(string name)
            {
                client.Folders[name].Messages.Download();
                return client.Folders[name].Messages;
            }
        }

        private void RemoveText(object sender, RoutedEventArgs e)
        {
            TextBox textBox = sender as TextBox;
            if (textBox.Text == "Введите почту" || textBox.Text == "Введите пароль")
            {
                textBox.Text = "";
            }
        }

        private void AddText(object sender, RoutedEventArgs e)
        {
            TextBox textBox = sender as TextBox;
            if (string.IsNullOrWhiteSpace(textBox.Text))
            {
                if (textBox.Name == "EmailTxt")
                {
                    textBox.Text = "Введите почту";
                }
            }
        }

        private void PasswordChanged(object sender, RoutedEventArgs e)
        {
            PasswordBox passwordBox = sender as PasswordBox;
            if (passwordBox.Password.Length > 0 && passwordBox.Password == "Введите пароль")
            {
                passwordBox.Password = "";
            }
        }

        private void Button_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                ImapHelper.Initialize((MailClientCbx.SelectedItem as ComboBoxItem).Tag.ToString());
                if (ImapHelper.Login(EmailTxt.Text, PasswordTxt.Password))
                {
                    MessageBox.Show("Вход выполнен успешно", "Успех", MessageBoxButton.OK, MessageBoxImage.Information);
                    Otpravka otpravka = new Otpravka();
                    otpravka.Show();
                    this.Close();
                }
                else
                {
                    MessageBox.Show("Ошибка входа. Проверьте учетные данные.", "Ошибка", MessageBoxButton.OK, MessageBoxImage.Error);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Ошибка: {ex.Message}", "Ошибка", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
    }
}
